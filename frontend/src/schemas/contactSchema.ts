import { z } from 'zod';
import { CONTACT_TYPE_OPTIONS } from './personSchema';

export { CONTACT_TYPE_OPTIONS };

const PHONE_PATTERN = /^[0-9+()\-\s]{7,20}$/;

export const contactFormSchema = z
  .object({
    personId: z.string().trim().min(1, 'Person is required'),
    phoneNumber: z
      .string()
      .trim()
      .min(1, 'Phone number is required')
      .regex(PHONE_PATTERN, 'Enter a valid phone number'),
    contactType: z.string().min(1, 'Contact type is required'),
  })
  .superRefine((data, ctx) => {
    if (data.contactType && !CONTACT_TYPE_OPTIONS.includes(data.contactType as (typeof CONTACT_TYPE_OPTIONS)[number])) {
      ctx.addIssue({ code: 'custom', message: 'Select a valid contact type', path: ['contactType'] });
    }
  });

export type ContactFormValues = z.infer<typeof contactFormSchema>;
