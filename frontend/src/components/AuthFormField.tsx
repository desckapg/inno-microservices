import {type ChangeEvent} from 'react'

type AuthFormFieldProps = {
  id: string
  label: string
  type: string
  placeholder: string
  value: string
  error?: string
  disabled: boolean
  autoComplete?: string
  onChange: (e: ChangeEvent<HTMLInputElement>) => void
}

export function AuthFormField({
  id,
  label,
  type,
  placeholder,
  value,
  error,
  disabled,
  autoComplete,
  onChange
}: Readonly<AuthFormFieldProps>) {
  return (
      <div className="mb-3">
        <label htmlFor={id} className="form-label fw-semibold">
          {label}
        </label>
        <input
            type={type}
            className={`form-control form-control-lg ${error ? 'is-invalid' : ''}`}
            id={id}
            placeholder={placeholder}
            value={value}
            onChange={onChange}
            disabled={disabled}
            autoComplete={autoComplete}
            aria-invalid={Boolean(error)}
            aria-describedby={error ? `${id}Error` : undefined}
        />
        {error ? (
            <div id={`${id}Error`} className="invalid-feedback">
              {error}
            </div>
        ) : null}
      </div>
  )
}
