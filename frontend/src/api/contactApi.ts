import axiosClient from './axiosClient';
import type {
  ContactCreateRequest,
  ContactResponse,
  ContactUpdateRequest,
  PageResponse,
} from './types';

export interface GetContactsParams {
  page?: number;
  size?: number;
  sort?: string;
  search?: string;
}

export async function createContact(data: ContactCreateRequest): Promise<ContactResponse> {
  const response = await axiosClient.post<ContactResponse>('/contacts', data);
  return response.data;
}

export async function getContacts(params: GetContactsParams = {}): Promise<PageResponse<ContactResponse>> {
  const response = await axiosClient.get<PageResponse<ContactResponse>>('/contacts', { params });
  return response.data;
}

export async function getContactById(id: number): Promise<ContactResponse> {
  const response = await axiosClient.get<ContactResponse>(`/contacts/${id}`);
  return response.data;
}

export async function updateContact(id: number, data: ContactUpdateRequest): Promise<ContactResponse> {
  const response = await axiosClient.put<ContactResponse>(`/contacts/${id}`, data);
  return response.data;
}

export async function deleteContact(id: number): Promise<void> {
  await axiosClient.delete(`/contacts/${id}`);
}
