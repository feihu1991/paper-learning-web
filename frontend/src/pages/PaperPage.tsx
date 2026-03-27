import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Row, Col, Card, Button, Spin, message, Typography, Space, Progress,
  Descriptions, Empty, Modal, Divider, Alert
} from 'antd';
import {
  ArrowLeftOutlined, ThunderboltOutlined, ExperimentOutlined,
  DeleteOutlined, CheckCircleOutlined
} from '@ant-design/icons';
import { paperApi, learningApi, configApi } from '../services/api';
import StepCard from '../components/StepCard';
import type { Paper, LearningStep, UserProgress, LlmConfig } from '../types';

const { Title, Paragraph } = Typography;

const PaperPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const paperId = Number(id);

  const [paper, setPaper] = useState<Paper | null>(null);
  const [steps, setSteps] = useState<LearningStep[]>([]);
  const [progress, setProgress] = useState<UserProgress | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [activeConfig, setActiveConfig] = useState<LlmConfig | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    loadData();
  }, [paperId]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [paperRes, stepsRes, progressRes, configRes] = await Promise.all([
        paperApi.getById(paperId),
        learningApi.getSteps(paperId),
        learningApi.getProgress(paperId).catch(() => null),
        configApi.getActiveLlmConfig(),
      ]);

      if (paperRes.success) {
        setPaper(paperRes.data);
      } else {
        message.error('论文不存在');
        navigate('/');
        return;
      }

      if (stepsRes.success) {
        setSteps(stepsRes.data || []);
      }

      if (progressRes?.success) {
        setProgress(progressRes.data);
      }

      if (configRes.success && configRes.data) {
        setActiveConfig(configRes.data);
      }
    } catch (e) {
      message.error('加载失败');
    } finally {
      setLoading(false);
    }
  };

  const handleParse = async () => {
    if (!activeConfig) {
      message.warning('请先配置 LLM');
      navigate('/settings');
      return;
    }
    try {
      setActionLoading(true);
      const res = await paperApi.parsePaper(paperId);
      if (res.success) {
        setPaper(res.data);
        message.success('解析完成');
        await loadData();
      } else {
        message.error(res.message || '解析失败');
      }
    } catch (e) {
      message.error('解析失败');
    } finally {
      setActionLoading(false);
    }
  };

  const handleGeneratePath = async () => {
    if (!activeConfig) {
      message.warning('请先配置 LLM');
      navigate('/settings');
      return;
    }
    try {
      setActionLoading(true);
      const res = await paperApi.generateLearningPath(paperId);
      if (res.success) {
        message.success('学习路径生成成功');
        await loadData();
      } else {
        message.error(res.message || '生成失败');
      }
    } catch (e) {
      message.error('生成失败');
    } finally {
      setActionLoading(false);
    }
  };

  const handleToggleStep = async (step: LearningStep) => {
    if (!step.id) return;
    const isCompleted = progress?.completedStepIds?.includes(step.id);
    try {
      const res = isCompleted
        ? await learningApi.uncompleteStep(paperId, step.id)
        : await learningApi.completeStep(paperId, step.id);
      if (res.success) {
        setProgress(res.data);
      }
    } catch (e) {
      message.error('更新失败');
    }
  };

  const handleDelete = async () => {
    try {
      await paperApi.delete(paperId);
      message.success('删除成功');
      navigate('/');
    } catch (e) {
      message.error('删除失败');
    }
  };

  const handleReset = async () => {
    try {
      const res = await learningApi.resetProgress(paperId);
      if (res.success) {
        setProgress(res.data);
        message.success('进度已重置');
      }
    } catch (e) {
      message.error('重置失败');
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 48 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!paper) {
    return <Empty description="论文不存在" />;
  }

  const completedCount = progress?.completedStepIds?.length || 0;
  const totalCount = steps.length || 0;
  const percent = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0;

  return (
    <div>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/')} style={{ marginBottom: 16 }}>
        返回
      </Button>

      <Row gutter={[24, 24]}>
        <Col xs={24} lg={paper.structuredSummary ? 12 : 24}>
          <Card
            title={
              <Title level={4} style={{ margin: 0 }}>
                {paper.title}
              </Title>
            }
            extra={
              <Space>
                <Button
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => setShowDeleteModal(true)}
                >
                  删除
                </Button>
              </Space>
            }
          >
            {paper.authors && (
              <Descriptions column={1} size="small">
                <Descriptions.Item label="作者">{paper.authors}</Descriptions.Item>
                {paper.arxivId && <Descriptions.Item label="ArXiv ID">{paper.arxivId}</Descriptions.Item>}
              </Descriptions>
            )}

            {paper.paperAbstract && (
              <>
                <Divider orientation="left">摘要</Divider>
                <Paragraph>{paper.paperAbstract}</Paragraph>
              </>
            )}

            <Divider />

            <Space style={{ marginBottom: 16 }}>
              {!activeConfig && (
                <Alert
                  type="warning"
                  message="请先配置 LLM API"
                  action={
                    <Button size="small" onClick={() => navigate('/settings')}>
                      去配置
                    </Button>
                  }
                  style={{ marginBottom: 8 }}
                />
              )}
              <Button
                type="primary"
                icon={<ThunderboltOutlined />}
                onClick={handleParse}
                loading={actionLoading}
                disabled={paper.parsedStatus === 'PARSING'}
              >
                {paper.parsedStatus === 'COMPLETED' ? '重新解析' : 'AI 解析'}
              </Button>
              <Button
                icon={<ExperimentOutlined />}
                onClick={handleGeneratePath}
                loading={actionLoading}
                disabled={!activeConfig}
              >
                生成学习路径
              </Button>
            </Space>
          </Card>
        </Col>

        {paper.structuredSummary && (
          <Col xs={24} lg={12}>
            <Card title="AI 结构化总结">
              <div
                className="parsed-content"
                dangerouslySetInnerHTML={{ __html: formatSummary(paper.structuredSummary) }}
              />
            </Card>
          </Col>
        )}
      </Row>

      {steps.length > 0 && (
        <Card style={{ marginTop: 24 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <Title level={5} style={{ margin: 0 }}>
              学习步骤 ({completedCount}/{totalCount})
            </Title>
            <Space>
              {progress && completedCount > 0 && (
                <Button size="small" onClick={handleReset}>
                  重置进度
                </Button>
              )}
            </Space>
          </div>
          <Progress percent={percent} status={percent === 100 ? 'success' : 'active'} />
          {steps.map(step => (
            <StepCard
              key={step.id}
              step={step}
              completed={progress?.completedStepIds?.includes(step.id || 0) || false}
              onToggle={() => handleToggleStep(step)}
            />
          ))}
          {percent === 100 && (
            <div style={{ textAlign: 'center', marginTop: 16 }}>
              <CheckCircleOutlined style={{ fontSize: 48, color: '#52c41a' }} />
              <Title level={4} style={{ color: '#52c41a', marginTop: 8 }}>
                太棒了！你已完成这篇论文的学习！
              </Title>
            </div>
          )}
        </Card>
      )}

      <Modal
        title="确认删除"
        open={showDeleteModal}
        onOk={handleDelete}
        onCancel={() => setShowDeleteModal(false)}
        okText="删除"
        okButtonProps={{ danger: true }}
      >
        <p>确定要删除这篇论文吗？此操作不可恢复。</p>
      </Modal>
    </div>
  );
};

function formatSummary(text: string): string {
  return text
    .replace(/##\s*([^#\n]+)/g, '<h2>$1</h2>')
    .replace(/\n/g, '<br/>');
}

export default PaperPage;
