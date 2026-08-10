import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate, useParams } from 'react-router-dom';
import { createContact, getContactById, updateContact } from '../../api/contactApi';
import { getPersons } from '../../api/personApi';
import type { ContactType, PersonResponse } from '../../api/types';
import { getErrorMessage } from '../../api/axiosClient';
import { CONTACT_TYPE_OPTIONS, contactFormSchema, type ContactFormValues } from '../../schemas/contactSchema';
import Banner from '../../components/common/Banner';

const emptyValues: ContactFormValues = {
  personId: '',
  phoneNumber: '',
  contactType: '',
};

export default function ContactFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [persons, setPersons] = useState<PersonResponse[]>([]);
  const [loadingPersons, setLoadingPersons] = useState(true);
  const [loadingContact, setLoadingContact] = useState(isEdit);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ContactFormValues>({
    resolver: zodResolver(contactFormSchema),
    defaultValues: emptyValues,
  });

  useEffect(() => {
    let cancelled = false;
    getPersons({ page: 0, size: 200, sort: 'firstName,asc' })
      .then((result) => {
        if (!cancelled) setPersons(result.content);
      })
      .catch((err: unknown) => {
        if (!cancelled) setLoadError(getErrorMessage(err));
      })
      .finally(() => {
        if (!cancelled) setLoadingPersons(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    setLoadingContact(true);
    getContactById(Number(id))
      .then((contact) => {
        if (cancelled) return;
        reset({
          personId: String(contact.personId),
          phoneNumber: contact.phoneNumber,
          contactType: contact.contactType,
        });
      })
      .catch((err: unknown) => {
        if (!cancelled) setLoadError(getErrorMessage(err));
      })
      .finally(() => {
        if (!cancelled) setLoadingContact(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id, reset]);

  async function onSubmit(values: ContactFormValues) {
    setSubmitError(null);
    const payload = {
      personId: Number(values.personId),
      phoneNumber: values.phoneNumber,
      contactType: values.contactType as ContactType,
    };
    try {
      if (isEdit && id) {
        await updateContact(Number(id), payload);
        navigate('/contacts', { state: { success: 'Contact updated successfully.' } });
      } else {
        await createContact(payload);
        navigate('/contacts', { state: { success: 'Contact created successfully.' } });
      }
    } catch (err) {
      setSubmitError(getErrorMessage(err));
    }
  }

  if (loadingPersons || loadingContact) {
    return <p className="text-sm text-gray-500">Loading...</p>;
  }

  return (
    <div className="max-w-xl">
      <h1 className="mb-6 text-2xl font-semibold text-gray-900">{isEdit ? 'Edit Contact' : 'Add Contact'}</h1>

      {loadError && <Banner variant="error" message={loadError} />}
      {submitError && <Banner variant="error" message={submitError} onDismiss={() => setSubmitError(null)} />}

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-5 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <div>
          <label htmlFor="personId" className="block text-sm font-medium text-gray-700">
            Person Name
          </label>
          <select
            id="personId"
            {...register('personId')}
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          >
            <option value="">Select a person...</option>
            {persons.map((person) => (
              <option key={person.id} value={person.id}>
                {person.firstName} {person.lastName}
              </option>
            ))}
          </select>
          {errors.personId && <p className="mt-1 text-sm text-red-600">{errors.personId.message}</p>}
          {persons.length === 0 && (
            <p className="mt-1 text-sm text-gray-500">No persons exist yet. Add a person first.</p>
          )}
        </div>

        <div>
          <label htmlFor="phoneNumber" className="block text-sm font-medium text-gray-700">
            Phone Number
          </label>
          <input
            id="phoneNumber"
            type="text"
            {...register('phoneNumber')}
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
          {errors.phoneNumber && <p className="mt-1 text-sm text-red-600">{errors.phoneNumber.message}</p>}
        </div>

        <div>
          <label htmlFor="contactType" className="block text-sm font-medium text-gray-700">
            Contact Type
          </label>
          <select
            id="contactType"
            {...register('contactType')}
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          >
            <option value="">Select type...</option>
            {CONTACT_TYPE_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
          {errors.contactType && <p className="mt-1 text-sm text-red-600">{errors.contactType.message}</p>}
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={() => navigate('/contacts')}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
          >
            {isSubmitting ? 'Saving...' : 'Save'}
          </button>
        </div>
      </form>
    </div>
  );
}
