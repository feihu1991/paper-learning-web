package com.paperlearning.service;

import com.paperlearning.dto.ArxivSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ArxivService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String ARXIV_API = "http://export.arxiv.org/api/query?";

    public List<ArxivSearchResult> search(String query, int maxResults) {
        try {
            String url = ARXIV_API + "search_query=all:" + query.replace(" ", "+")
                       + "&max_results=" + maxResults + "&sortBy=relevance";
            String xml = restTemplate.getForObject(url, String.class);
            return parseArxivResponse(xml);
        } catch (Exception e) {
            log.error("ArXiv search failed for query: {}", query, e);
            return Collections.emptyList();
        }
    }

    public Optional<ArxivSearchResult> getById(String arxivId) {
        try {
            String url = ARXIV_API + "id_list=" + arxivId;
            String xml = restTemplate.getForObject(url, String.class);
            List<ArxivSearchResult> results = parseArxivResponse(xml);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            log.error("ArXiv fetch failed for id: {}", arxivId, e);
            return Optional.empty();
        }
    }

    private List<ArxivSearchResult> parseArxivResponse(String xml) throws Exception {
        List<ArxivSearchResult> results = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        NodeList entries = doc.getElementsByTagName("entry");

        for (int i = 0; i < entries.getLength(); i++) {
            Node entry = entries.item(i);
            NamedNodeMap attrs = entry.getAttributes();

            String id = getTextContent(entry, "id");
            String arxivId = id.substringAfterLast("/");

            String title = cleanText(getTextContent(entry, "title"));
            String summary = cleanText(getTextContent(entry, "summary"));
            String published = getTextContent(entry, "published");

            List<String> authors = new ArrayList<>();
            NodeList children = entry.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if ("author".equals(child.getNodeName())) {
                    authors.add(getTextContent(child, "name"));
                }
            }

            String pdfUrl = "";
            NodeList links = entry.getChildNodes();
            for (int j = 0; j < links.getLength(); j++) {
                Node link = links.item(j);
                if ("link".equals(link.getNodeName())) {
                    Node type = link.getAttributes().getNamedItem("title");
                    if (type != null && "pdf".equals(type.getTextContent())) {
                        pdfUrl = link.getAttributes().getNamedItem("href").getTextContent();
                    }
                }
            }

            results.add(ArxivSearchResult.builder()
                    .arxivId(arxivId)
                    .title(title)
                    .authors(String.join(", ", authors))
                    .abstract_(summary)
                    .published(published)
                    .pdfUrl(pdfUrl.isEmpty() ? id.replace("abs", "pdf") + ".pdf" : pdfUrl)
                    .build());
        }
        return results;
    }

    private String getTextContent(Node parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (tagName.equals(child.getNodeName())) {
                return child.getTextContent();
            }
        }
        return "";
    }

    private String cleanText(String text) {
        if (text == null) return "";
        return text.replaceAll("\\s+", " ").trim();
    }
}
