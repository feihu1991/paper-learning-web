import { Card, Tag, Typography } from 'antd';
import { FilePdfOutlined, CheckCircleOutlined, ClockCircleOutlined } from '@ant-design/icons';
import type { Paper } from '../types';

const { Text } = Typography;

interface Props {
  paper: Paper;
  onClick: () => void;
}

const statusColor: Record<string, string> = {
  NOT_PARSED: 'default',
  PARSING: 'processing',
  COMPLETED: 'success',
  FAILED: 'error',
};

const statusText: Record<string, string> = {
  NOT_PARSED: '未解析',
  PARSING: '解析中',
  COMPLETED: '已解析',
  FAILED: '解析失败',
};

const PaperCard = ({ paper, onClick }: Props) => {
  const status = paper.parsedStatus || 'NOT_PARSED';

  return (
    <Card className="paper-card" onClick={onClick} hoverable>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div style={{ flex: 1 }}>
          <Text strong style={{ fontSize: 16, display: 'block', marginBottom: 8 }}>
            {paper.title}
          </Text>
          {paper.authors && (
            <Text type="secondary" style={{ fontSize: 13, display: 'block', marginBottom: 8 }}>
              {paper.authors.length > 100 ? paper.authors.substring(0, 100) + '...' : paper.authors}
            </Text>
          )}
          {paper.paperAbstract && (
            <Text style={{ fontSize: 13, color: '#595959' }}>
              {paper.paperAbstract.length > 150 ? paper.paperAbstract.substring(0, 150) + '...' : paper.paperAbstract}
            </Text>
          )}
        </div>
      </div>
      <div style={{ marginTop: 12, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        {paper.arxivId && (
          <Tag icon={<FilePdfOutlined />} color="blue">
            {paper.arxivId}
          </Tag>
        )}
        <Tag color={statusColor[status]}>
          {status === 'COMPLETED' ? <CheckCircleOutlined /> : status === 'PARSING' ? <ClockCircleOutlined /> : null}
          {statusText[status]}
        </Tag>
      </div>
    </Card>
  );
};

export default PaperCard;
