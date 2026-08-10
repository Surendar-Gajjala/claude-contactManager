export type Gender = 'MALE' | 'FEMALE' | 'OTHER';
export type ContactType = 'PERSONAL' | 'HOME' | 'WORK' | 'OTHER';

export interface PersonResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  gender: Gender;
  address: string | null;
  createdDate: string;
  updatedDate: string;
}

export interface PersonCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  gender: Gender;
  address?: string;
}

export type PersonUpdateRequest = PersonCreateRequest;

export interface ContactResponse {
  id: number;
  personId: number;
  personName: string;
  phoneNumber: string;
  contactType: ContactType;
  createdDate: string;
  updatedDate: string;
}

export interface ContactCreateRequest {
  personId: number;
  phoneNumber: string;
  contactType: ContactType;
}

export type ContactUpdateRequest = ContactCreateRequest;

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ExcelImportResponse {
  personsCreated: number;
  contactsCreated: number;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: Record<string, string>;
  details?: string[];
}
