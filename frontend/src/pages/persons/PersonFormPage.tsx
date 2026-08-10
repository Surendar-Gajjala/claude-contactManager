import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate, useParams } from 'react-router-dom';
import { createPerson, getPersonById, updatePerson } from '../../api/personApi';
import { createContact } from '../../api/contactApi';
import { getErrorMessage } from '../../api/axiosClient';
import { GENDER_OPTIONS, CONTACT_TYPE_OPTIONS, personFormSchema, type PersonFormValues } from '../../schemas/personSchema';
import Banner from '../../components/common/Banner';
import type { ContactType, Gender } from '../../api/types';

const emptyValues: PersonFormValues = {
  firstName: '',
  lastName: '',
  email: '',
  gender: '',
  address: '',
  phoneNumber: '',
  contactType: '',
};

export default function PersonFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [loadingPerson, setLoadingPerson] = useState(isEdit);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<PersonFormValues>({
    resolver: zodResolver(personFormSchema),
    defaultValues: emptyValues,
  });

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    setLoadingPerson(true);
    getPersonById(Number(id))
      .then((person) => {
        if (cancelled) return;
        reset({
          firstName: person.firstName,
          lastName: person.lastName,
          email: person.email,
          gender: person.gender,
          address: person.address ?? '',
          phoneNumber: '',
          contactType: '',
        });
      })
      .catch((err: unknown) => {
        if (!cancelled) setLoadError(getErrorMessage(err));
      })
      .finally(() => {
        if (!cancelled) setLoadingPerson(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id, reset]);

  async function onSubmit(values: PersonFormValues) {
    setSubmitError(null);
    try {
      if (isEdit && id) {
        await updatePerson(Number(id), {
          firstName: values.firstName,
          lastName: values.lastName,
          email: values.email,
          gender: values.gender as Gender,
          address: values.address || undefined,
        });
        navigate('/persons', { state: { success: 'Person updated successfully.' } });
        return;
      }

      const person = await createPerson({
        firstName: values.firstName,
        lastName: values.lastName,
        email: values.email,
        gender: values.gender as Gender,
        address: values.address || undefined,
      });

      if (values.phoneNumber && values.contactType) {
        try {
          await createContact({
            personId: person.id,
            phoneNumber: values.phoneNumber,
            contactType: values.contactType as ContactType,
          });
        } catch (contactErr) {
          navigate('/persons', {
            state: {
              warning: `${person.firstName} ${person.lastName} was created, but the contact could not be saved: ${getErrorMessage(contactErr)}`,
            },
          });
          return;
        }
      }

      navigate('/persons', { state: { success: 'Person created successfully.' } });
    } catch (err) {
      setSubmitError(getErrorMessage(err));
    }
  }

  if (loadingPerson) {
    return <p className="text-sm text-gray-500">Loading person...</p>;
  }

  return (
    <div className="max-w-xl">
      <h1 className="mb-6 text-2xl font-semibold text-gray-900">{isEdit ? 'Edit Person' : 'Add Person'}</h1>

      {loadError && <Banner variant="error" message={loadError} />}
      {submitError && <Banner variant="error" message={submitError} onDismiss={() => setSubmitError(null)} />}

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-5 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label htmlFor="firstName" className="block text-sm font-medium text-gray-700">
              First Name
            </label>
            <input
              id="firstName"
              type="text"
              {...register('firstName')}
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
            {errors.firstName && <p className="mt-1 text-sm text-red-600">{errors.firstName.message}</p>}
          </div>
          <div>
            <label htmlFor="lastName" className="block text-sm font-medium text-gray-700">
              Last Name
            </label>
            <input
              id="lastName"
              type="text"
              {...register('lastName')}
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
            {errors.lastName && <p className="mt-1 text-sm text-red-600">{errors.lastName.message}</p>}
          </div>
        </div>

        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700">
            Email
          </label>
          <input
            id="email"
            type="email"
            {...register('email')}
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
          {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>}
        </div>

        <div>
          <label htmlFor="address" className="block text-sm font-medium text-gray-700">
            Address
          </label>
          <input
            id="address"
            type="text"
            {...register('address')}
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
          {errors.address && <p className="mt-1 text-sm text-red-600">{errors.address.message}</p>}
        </div>

        <div>
          <label htmlFor="gender" className="block text-sm font-medium text-gray-700">
            Gender
          </label>
          <select
            id="gender"
            {...register('gender')}
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          >
            <option value="">Select gender...</option>
            {GENDER_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
          {errors.gender && <p className="mt-1 text-sm text-red-600">{errors.gender.message}</p>}
        </div>

        {!isEdit && (
          <div className="grid grid-cols-2 gap-4 border-t border-gray-100 pt-5">
            <div>
              <label htmlFor="phoneNumber" className="block text-sm font-medium text-gray-700">
                Phone Number
              </label>
              <input
                id="phoneNumber"
                type="text"
                placeholder="Optional"
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
          </div>
        )}
        {isEdit && (
          <p className="border-t border-gray-100 pt-4 text-sm text-gray-500">
            Manage this person's phone numbers from the Contacts page.
          </p>
        )}

        <div className="flex justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={() => navigate('/persons')}
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
