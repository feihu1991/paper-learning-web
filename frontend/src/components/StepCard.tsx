import { Card, Checkbox, Tag, Typography } from 'antd';
import type { LearningStep } from '../types';

const { Text } = Typography;

interface Props {
  step: LearningStep;
  completed: boolean;
  onToggle: () => void;
}

const typeColors: Record<string, string> = {
  BACKGROUND: 'purple',
  METHOD: 'blue',
  EXPERIMENT: 'green',
  CONCLUSION: 'orange',
  DISCUSSION: 'cyan',
};

const typeText: Record<string, string> = {
  BACKGROUND: '背景',
  METHOD: '方法',
  EXPERIMENT: '实验',
  CONCLUSION: '结论',
  DISCUSSION: '讨论',
};

const StepCard = ({ step, completed, onToggle }: Props) => {
  return (
    <Card
      size="small"
      className="step-card"
      style={{
        opacity: completed ? 0.7 : 1,
        background: completed ? '#f5f5f5' : '#fff',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
        <Checkbox checked={completed} onChange={onToggle} style={{ marginTop: 2 }} />
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
            <Text strong delete={completed}>
              {step.title}
            </Text>
            <Tag color={typeColors[step.type]} className="step-type-badge">
              {typeText[step.type]}
            </Tag>
            {step.estimatedMinutes && (
              <Text type="secondary" style={{ fontSize: 12 }}>
                ~{step.estimatedMinutes}分钟
              </Text>
            )}
          </div>
          <Text style={{ fontSize: 13, color: '#595959' }}>
            {step.content}
          </Text>
        </div>
      </div>
    </Card>
  );
};

export default StepCard;
