import { useState, useEffect } from 'react';
import {
  Card, Table, Button, Space, Modal, Form, Input, Select, Tag, message,
  Popconfirm, Typography, Alert
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { configApi } from '../services/api';
import type { LlmConfig } from '../types';

const { Title, Text } = Typography;
const { Option } = Select;

const PRESET_CONFIGS: Partial<LlmConfig>[] = [
  {
    name: 'OpenAI (GPT-4)',
    apiEndpoint: 'https://api.openai.com/v1/',
    model: 'gpt-4',
    preset: true,
  },
  {
    name: 'OpenAI (GPT-3.5)',
    apiEndpoint: 'https://api.openai.com/v1/',
    model: 'gpt-3.5-turbo',
    preset: true,
  },
  {
    name: 'Claude (Anthropic)',
    apiEndpoint: 'https://api.anthropic.com/v1/',
    model: 'claude-3-opus-20240229',
    preset: true,
  },
  {
    name: 'DeepSeek',
    apiEndpoint: 'https://api.deepseek.com/v1/',
    model: 'deepseek-chat',
    preset: true,
  },
  {
    name: '通义千问',
    apiEndpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1/',
    model: 'qwen-plus',
    preset: true,
  },
];

const SettingsPage = () => {
  const [configs, setConfigs] = useState<LlmConfig[]>([]);
  const [activeConfig, setActiveConfig] = useState<LlmConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingConfig, setEditingConfig] = useState<LlmConfig | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadConfigs();
  }, []);

  const loadConfigs = async () => {
    try {
      setLoading(true);
      const [allRes, activeRes] = await Promise.all([
        configApi.getAllLlmConfigs(),
        configApi.getActiveLlmConfig(),
      ]);
      if (allRes.success) {
        setConfigs(allRes.data || []);
      }
      if (activeRes.success) {
        setActiveConfig(activeRes.data || null);
      }
    } catch (e) {
      message.error('加载配置失败');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingConfig(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (config: LlmConfig) => {
    setEditingConfig(config);
    form.setFieldsValue(config);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      const res = await configApi.deleteLlmConfig(id);
      if (res.success) {
        message.success('删除成功');
        loadConfigs();
      }
    } catch (e) {
      message.error('删除失败');
    }
  };

  const handleActivate = async (id: number) => {
    try {
      const res = await configApi.activateLlmConfig(id);
      if (res.success) {
        message.success('已激活');
        loadConfigs();
      }
    } catch (e) {
      message.error('激活失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const configData: LlmConfig = {
        ...values,
        apiKeyAlias: values.apiKeyAlias || values.apiKey,
      };

      let res;
      if (editingConfig?.id) {
        res = await configApi.updateLlmConfig(editingConfig.id, configData);
      } else {
        res = await configApi.createLlmConfig(configData);
      }

      if (res.success) {
        message.success(editingConfig?.id ? '更新成功' : '创建成功');
        setModalVisible(false);
        loadConfigs();
      }
    } catch (e) {
      message.error('操作失败');
    }
  };

  const handlePresetSelect = (name: string) => {
    const preset = PRESET_CONFIGS.find(p => p.name === name);
    if (preset) {
      form.setFieldsValue(preset);
    }
  };

  const columns = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'API 端点',
      dataIndex: 'apiEndpoint',
      key: 'apiEndpoint',
      render: (text: string) => <Text code>{text}</Text>,
    },
    {
      title: '模型',
      dataIndex: 'model',
      key: 'model',
    },
    {
      title: '状态',
      dataIndex: 'active',
      key: 'active',
      render: (active: boolean) =>
        active ? <Tag color="green" icon={<CheckCircleOutlined />}>使用中</Tag> : <Tag>未激活</Tag>,
    },
    {
      title: '操作',
      key: 'action',
      render: (_: unknown, record: LlmConfig) => (
        <Space>
          <Button
            type="text"
            icon={<CheckCircleOutlined />}
            onClick={() => handleActivate(record.id!)}
            disabled={record.active}
          >
            激活
          </Button>
          <Button type="text" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          {!record.preset && (
            <Popconfirm
              title="确定删除？"
              onConfirm={() => handleDelete(record.id!)}
            >
              <Button type="text" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Title level={3}>LLM 配置</Title>
      <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
        配置您要使用的 LLM API（OpenAI、Claude、DeepSeek 等）。激活后，系统将使用此配置进行论文解析和学习路径生成。
      </Text>

      {activeConfig ? (
        <Alert
          type="success"
          message={`当前使用: ${activeConfig.name} (${activeConfig.model})`}
          style={{ marginBottom: 16 }}
        />
      ) : (
        <Alert
          type="warning"
          message="未配置 LLM，请添加并激活配置后才能使用 AI 解析功能"
          style={{ marginBottom: 16 }}
        />
      )}

      <Card
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            添加配置
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={configs}
          rowKey="id"
          loading={loading}
          pagination={false}
        />
      </Card>

      <Modal
        title={editingConfig ? '编辑配置' : '添加配置'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="保存"
      >
        <Form form={form} layout="vertical" initialValues={{ active: false }}>
          {!editingConfig && (
            <Form.Item label="预设配置" name="presetSelect">
              <Select
                placeholder="选择预设快速填充"
                onChange={handlePresetSelect}
                allowClear
              >
                {PRESET_CONFIGS.map(p => (
                  <Option key={p.name} value={p.name!}>
                    {p.name}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          )}

          <Form.Item
            label="配置名称"
            name="name"
            rules={[{ required: true, message: '请输入配置名称' }]}
          >
            <Input placeholder="例如：我的 GPT-4 配置" />
          </Form.Item>

          <Form.Item
            label="API 端点"
            name="apiEndpoint"
            rules={[{ required: true, message: '请输入 API 端点' }]}
          >
            <Input placeholder="https://api.openai.com/v1/" />
          </Form.Item>

          <Form.Item
            label="模型名称"
            name="model"
            rules={[{ required: true, message: '请输入模型名称' }]}
          >
            <Input placeholder="gpt-4" />
          </Form.Item>

          <Form.Item
            label="API Key"
            name="apiKey"
            rules={[{ required: !editingConfig, message: '请输入 API Key' }]}
          >
            <Input.Password placeholder="输入 API Key" />
          </Form.Item>

          {editingConfig && (
            <Form.Item label="设为激活" name="active" valuePropName="checked">
              <input type="checkbox" />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
};

export default SettingsPage;
