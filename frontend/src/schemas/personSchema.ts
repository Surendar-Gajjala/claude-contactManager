import { z } from 'zod';

export const GENDER_OPTIONS = ['MALE', 'FEMALE', 'OTHER'] as const;
export const CONTACT_TYPE_OPTIONS = ['PERSONAL', 'HOME', 'WORK', 'OTHER'] as const;

const PHONE_PATTERN = /^[0-9+()\-\s]{7,20}$/;

// Person + optional first Contact (cmsPrompt.txt section 6). Gender/contactType are kept as
// plain strings (not z.enum) so an empty '' default is valid at the type level for an
// unselected <select>, and validated explicitly below.
export const personFormSchema = z
  .object({
    firstName: z.string().trim().min(1, 'First name is required').max(100, 'Must be 100 characters or fewer'),
    lastName: z.string().trim().min(1, 'Last name is required').max(100, 'Must be 100 characters or fewer'),
    email: z.string().trim().min(1, 'Email is required').email('Enter a valid email address'),
    gender: z.string(),
    address: z.string().trim().max(255, 'Must be 255 characters or fewer').optional(),
    phoneNumber: z.string().trim().optional(),
    contactType: z.string().optional(),
  })
  .superRefine((data, ctx) => {
    if (!GENDER_OPTIONS.includes(data.gender as (typeof GENDER_OPTIONS)[number])) {
      ctx.addIssue({ code: 'custom', message: 'Gender is required', path: ['gender'] });
    }

    if (data.phoneNumber) {
      if (!PHONE_PATTERN.test(data.phoneNumber)) {
        ctx.addIssue({ code: 'custom', message: 'Enter a valid phone number', path: ['phoneNumber'] });
      }
      if (!data.contactType) {
        ctx.addIssue({
          code: 'custom',
          message: 'Contact type is required when a phone number is provided',
          path: ['contactType'],
        });
      } else if (!CONTACT_TYPE_OPTIONS.includes(data.contactType as (typeof CONTACT_TYPE_OPTIONS)[number])) {
        ctx.addIssue({ code: 'custom', message: 'Select a valid contact type', path: ['contactType'] });
      }
    }
  });

export type PersonFormValues = z.infer<typeof personFormSchema>;
