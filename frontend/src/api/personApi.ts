import axiosClient from './axiosClient';
import type {
  ExcelImportResponse,
  PageResponse,
  PersonCreateRequest,
  PersonResponse,
  PersonUpdateRequest,
} from './types';

export interface GetPersonsParams {
  page?: number;
  size?: number;
  sort?: string;
  search?: string;
}

export async function createPerson(data: PersonCreateRequest): Promise<PersonResponse> {
  const response = await axiosClient.post<PersonResponse>('/persons', data);
  return response.data;
}

export async function getPersons(params: GetPersonsParams = {}): Promise<PageResponse<PersonResponse>> {
  const response = await axiosClient.get<PageResponse<PersonResponse>>('/persons', { params });
  return response.data;
}

export async function getPersonById(id: number): Promise<PersonResponse> {
  const response = await axiosClient.get<PersonResponse>(`/persons/${id}`);
  return response.data;
}

export async function updatePerson(id: number, data: PersonUpdateRequest): Promise<PersonResponse> {
  const response = await axiosClient.put<PersonResponse>(`/persons/${id}`, data);
  return response.data;
}

export async function deletePerson(id: number): Promise<void> {
  await axiosClient.delete(`/persons/${id}`);
}

export async function uploadExcel(file: File): Promise<ExcelImportResponse> {
  const formData = new FormData();
  formData.append('file', file);
  const response = await axiosClient.post<ExcelImportResponse>('/persons/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
}
