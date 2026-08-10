interface BannerProps {
  variant: 'success' | 'error';
  message: string;
  onDismiss?: () => void;
}

const VARIANT_CLASSES: Record<BannerProps['variant'], string> = {
  success: 'bg-green-50 text-green-800 border-green-200',
  error: 'bg-red-50 text-red-800 border-red-200',
};

export default function Banner({ variant, message, onDismiss }: BannerProps) {
  return (
    <div
      role="alert"
      className={`mb-4 flex items-start justify-between gap-4 rounded-md border px-4 py-3 text-sm ${VARIANT_CLASSES[variant]}`}
    >
      <span>{message}</span>
      {onDismiss && (
        <button type="button" onClick={onDismiss} className="font-medium underline underline-offset-2">
          Dismiss
        </button>
      )}
    </div>
  );
}
