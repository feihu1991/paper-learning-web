import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Row, Col, Input, Button, Spin, message, Empty, Modal, Typography, Space, Tag } from 'antd';
import { SearchOutlined, DownloadOutlined, ReloadOutlined } from '@ant-design/icons';
import { paperApi } from '../services/api';
import PaperCard from '../components/PaperCard';
import type { Paper, ArxivSearchResult } from '../types';

const { Text } = Typography;

const HomePage = () => {
  const navigate = useNavigate();
  const [papers, setPapers] = useState<Paper[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [arxivQuery, setArxivQuery] = useState('');
  const [arxivResults, setArxivResults] = useState<ArxivSearchResult[]>([]);
  const [arxivLoading, setArxivLoading] = useState(false);
  const [importing, setImporting] = useState<string | null>(null);
  const [showArxivModal, setShowArxivModal] = useState(false);

  useEffect(() => {
    loadPapers();
  }, []);

  const loadPapers = async () => {
    try {
      setLoading(true);
      const res = await paperApi.getAll();
      if (res.success) {
        setPapers(res.data || []);
      }
    } catch (e) {
      message.error('加载论文列表失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchKeyword.trim()) {
      loadPapers();
      return;
    }
    try {
      setLoading(true);
      const res = await paperApi.search(searchKeyword);
      if (res.success) {
        setPapers(res.data || []);
      }
    } catch (e) {
      message.error('搜索失败');
    } finally {
      setLoading(false);
    }
  };

  const handleArxivSearch = async () => {
    if (!arxivQuery.trim()) return;
    try {
      setArxivLoading(true);
      const res = await paperApi.searchArxiv(arxivQuery);
      if (res.success) {
        setArxivResults(res.data || []);
        if (!res.data?.length) {
          message.info('未找到相关论文');
        }
      }
    } catch (e) {
      message.error('ArXiv 搜索失败');
    } finally {
      setArxivLoading(false);
    }
  };

  const handleImportArxiv = async (result: ArxivSearchResult) => {
    try {
      setImporting(result.arxivId);
      const res = await paperApi.importArxiv(result);
      if (res.success && res.data) {
        message.success('导入成功');
        setShowArxivModal(false);
        loadPapers();
        navigate(`/paper/${res.data.id}`);
      } else {
        message.error(res.message || '导入失败');
      }
    } catch (e) {
      message.error('导入失败');
    } finally {
      setImporting(null);
    }
  };

  const handlePaperClick = (paper: Paper) => {
    if (paper.id) {
      navigate(`/paper/${paper.id}`);
    }
  };

  return (
    <div>
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={16}>
          <Input.Search
            placeholder="搜索论文标题、作者或摘要..."
            value={searchKeyword}
            onChange={e => setSearchKeyword(e.target.value)}
            onSearch={handleSearch}
            enterButton={
              <Button type="primary" icon={<SearchOutlined />}>
                搜索
              </Button>
            }
            size="large"
          />
        </Col>
        <Col xs={24} sm={8}>
          <Space style={{ width: '100%' }}>
            <Button
              icon={<DownloadOutlined />}
              onClick={() => setShowArxivModal(true)}
              size="large"
              style={{ width: '100%' }}
            >
              从 ArXiv 导入
            </Button>
            <Button icon={<ReloadOutlined />} onClick={loadPapers} size="large" />
          </Space>
        </Col>
      </Row>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 48 }}>
          <Spin size="large" />
        </div>
      ) : papers.length === 0 ? (
        <Empty description="暂无论文，请从 ArXiv 导入" />
      ) : (
        <Row gutter={[16, 16]}>
          {papers.map(paper => (
            <Col xs={24} md={12} lg={8} key={paper.id}>
              <PaperCard paper={paper} onClick={() => handlePaperClick(paper)} />
            </Col>
          ))}
        </Row>
      )}

      <Modal
        title="从 ArXiv 导入论文"
        open={showArxivModal}
        onCancel={() => setShowArxivModal(false)}
        footer={null}
        width={700}
      >
        <Input.Search
          placeholder="输入 ArXiv 论文标题或关键词搜索"
          value={arxivQuery}
          onChange={e => setArxivQuery(e.target.value)}
          onSearch={handleArxivSearch}
          enterButton={
            <Button type="primary" icon={<SearchOutlined />}>
              搜索
            </Button>
          }
          size="large"
          style={{ marginBottom: 16 }}
        />
        {arxivLoading ? (
          <div style={{ textAlign: 'center', padding: 24 }}>
            <Spin />
          </div>
        ) : (
          <div style={{ maxHeight: 400, overflowY: 'auto' }}>
            {arxivResults.map(result => (
              <div
                key={result.arxivId}
                className="arxiv-result-item"
                onClick={() => handleImportArxiv(result)}
              >
                <div className="search-result-title">{result.title}</div>
                <div className="search-result-authors">{result.authors}</div>
                <div className="search-result-summary">{result.summary}</div>
                <div style={{ marginTop: 8 }}>
                  <Tag color="blue">{result.arxivId}</Tag>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    发布于 {new Date(result.published).toLocaleDateString('zh-CN')}
                  </Text>
                </div>
                {importing === result.arxivId && (
                  <div style={{ marginTop: 8 }}>
                    <Spin size="small" /> 导入中...
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default HomePage;
