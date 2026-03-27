export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

export interface Paper {
  id?: number;
  arxivId?: string;
  title: string;
  authors?: string;
  paperAbstract?: string;
  pdfPath?: string;
  sourceUrl?: string;
  parsedStatus?: 'NOT_PARSED' | 'PARSING' | 'COMPLETED' | 'FAILED';
  structuredSummary?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface LearningStep {
  id?: number;
  paperId: number;
  title: string;
  type: 'BACKGROUND' | 'METHOD' | 'EXPERIMENT' | 'CONCLUSION' | 'DISCUSSION';
  content: string;
  estimatedMinutes?: number;
  orderIndex?: number;
}

export interface UserProgress {
  id?: number;
  paperId: number;
  completedStepIds: number[];
  currentStepId?: number;
  totalSteps: number;
  completedSteps: number;
  percentComplete?: number;
  updatedAt?: string;
}

export interface LlmConfig {
  id?: number;
  name: string;
  apiEndpoint: string;
  model: string;
  apiKeyAlias?: string;
  active?: boolean;
  preset?: boolean;
}

export interface ArxivSearchResult {
  arxivId: string;
  title: string;
  authors: string;
  summary: string;
  published: string;
  pdfUrl?: string;
}
