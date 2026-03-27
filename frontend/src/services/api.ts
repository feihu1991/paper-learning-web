import axios from 'axios';
import type { ApiResponse, Paper, LearningStep, UserProgress, LlmConfig, ArxivSearchResult } from '../types';

const API_BASE = '/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

// Paper APIs
export const paperApi = {
  getAll: () => api.get<ApiResponse<Paper[]>>('/papers').then(r => r.data),
  getById: (id: number) => api.get<ApiResponse<Paper>>(`/papers/${id}`).then(r => r.data),
  search: (keyword: string) => api.get<ApiResponse<Paper[]>>(`/papers/search?keyword=${encodeURIComponent(keyword)}`).then(r => r.data),
  save: (paper: Paper) => api.post<ApiResponse<Paper>>('/papers', paper).then(r => r.data),
  delete: (id: number) => api.delete<ApiResponse<void>>(`/papers/${id}`).then(r => r.data),
  searchArxiv: (query: string) => api.get<ApiResponse<ArxivSearchResult[]>>(`/papers/arxiv/search?query=${encodeURIComponent(query)}`).then(r => r.data),
  importArxiv: (result: ArxivSearchResult) => api.post<ApiResponse<Paper>>('/papers/arxiv/import', result).then(r => r.data),
  parsePaper: (id: number) => api.post<ApiResponse<Paper>>(`/papers/${id}/parse`).then(r => r.data),
  generateLearningPath: (id: number) => api.post<ApiResponse<Paper>>(`/papers/${id}/generate-learning-path`).then(r => r.data),
};

// Learning APIs
export const learningApi = {
  getSteps: (paperId: number) => api.get<ApiResponse<LearningStep[]>>(`/learning/papers/${paperId}/steps`).then(r => r.data),
  getProgress: (paperId: number) => api.get<ApiResponse<UserProgress>>(`/learning/papers/${paperId}/progress`).then(r => r.data),
  completeStep: (paperId: number, stepId: number) =>
    api.post<ApiResponse<UserProgress>>(`/learning/papers/${paperId}/steps/${stepId}/complete`).then(r => r.data),
  uncompleteStep: (paperId: number, stepId: number) =>
    api.post<ApiResponse<UserProgress>>(`/learning/papers/${paperId}/steps/${stepId}/uncomplete`).then(r => r.data),
  resetProgress: (paperId: number) => api.post<ApiResponse<UserProgress>>(`/learning/papers/${paperId}/reset`).then(r => r.data),
};

// Config APIs
export const configApi = {
  getAllLlmConfigs: () => api.get<ApiResponse<LlmConfig[]>>('/config/llm').then(r => r.data),
  getActiveLlmConfig: () => api.get<ApiResponse<LlmConfig | null>>('/config/llm/active').then(r => r.data),
  getLlmConfigById: (id: number) => api.get<ApiResponse<LlmConfig>>(`/config/llm/${id}`).then(r => r.data),
  createLlmConfig: (config: LlmConfig) => api.post<ApiResponse<LlmConfig>>('/config/llm', config).then(r => r.data),
  updateLlmConfig: (id: number, config: LlmConfig) => api.put<ApiResponse<LlmConfig>>(`/config/llm/${id}`, config).then(r => r.data),
  deleteLlmConfig: (id: number) => api.delete<ApiResponse<void>>(`/config/llm/${id}`).then(r => r.data),
  activateLlmConfig: (id: number) => api.post<ApiResponse<LlmConfig>>(`/config/llm/${id}/activate`).then(r => r.data),
};
