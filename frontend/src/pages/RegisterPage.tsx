import {type FormEvent, useMemo, useState} from 'react'
import {login as loginRequest} from '../api/services/auth-service'

type FieldErrors = {
  name?: string
  surname?: string
  email?: string
  birthDate?: string
  login?: string
  password?: string
  confirmedPassword?: string
}

function validate(values: { name: string, surname: string, email: string, birthDate: string, login: string; password: string, confirmedPassword: string }): FieldErrors {
  const errors: FieldErrors = {}

  const name = values.name.trim()
  const surname = values.surname.trim()
  const email = values.email
  const birthDate = Date.parse(values.birthDate)
  const login = values.login.trim()
  const password = values.password
  const confirmedPassword = values.confirmedPassword

  if (!name) errors.name = 'Names is required'

  if (!surname) errors.surname = 'Surname is required'

  if (!email) errors.email = 'Email is required'

  if (!birthDate) errors.birthDate = 'Birth date is required'

  if (!login) errors.login = 'Login is required.'
  else if (login.length < 3) errors.login = 'Login must be at least 3 characters.'

  if (!password) errors.password = 'Password is required.'

  if (!confirmedPassword) errors.confirmedPassword = 'Confirmed passwords is required'
  else if (password !== confirmedPassword) errors.confirmedPassword = 'Passwords do not match'

  return errors
}

function getErrorMessage(err: unknown): string {
  if (err instanceof Error) return err.message
  return 'Login failed. Please check your credentials and try again.'
}

export function RegisterPage() {
  const [name, setName] = useState('')
  const [surname, setSurname] = useState('')
  const [email, setEmail] = useState('')
  const [birthDate, setBirthDate] = useState('')
  const [login, setLogin] = useState('')
  const [password, setPassword] = useState('')
  const [confirmedPassword, setConfirmedPassword] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const [errors, setErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)

  const isSubmitDisabled = useMemo(() => {
    return isLoading
  }, [isLoading])

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setFormError(null)

    const nextErrors = validate({name, surname, email, birthDate, login, password, confirmedPassword})
    setErrors(nextErrors)

    if (Object.keys(nextErrors).length > 0) return

    setIsLoading(true)
    try {
      const response = await loginRequest({
        login: login.trim(),
        password,
      })

      console.log('Register success:', response)
      // TODO: store tokens, redirect, etc.
    } catch (err) {
      setFormError(getErrorMessage(err))
    } finally {
      setIsLoading(false)
    }
  }

  return (
      <div className="container">
        <div className="row justify-content-center align-items-center min-vh-100">
          <div className="col-12 col-sm-10 col-md-8 col-lg-5 col-xl-4">
            <div className="card shadow border-0">
              <div className="card-body p-4 p-md-5">
                <div className="text-center mb-4">
                  <h1 className="h3 fw-bold mb-3">Welcome Back</h1>
                  <p className="text-muted">Sign in to your account</p>
                </div>

                {formError ? (
                    <div className="alert alert-danger" role="alert">
                      {formError}
                    </div>
                ) : null}

                <form onSubmit={handleSubmit} noValidate>
                  <div className="mb-3">
                    <label htmlFor="name" className="form-label fw-semibold">
                      Name
                    </label>
                    <input
                        type="text"
                        className={`form-control form-control-lg ${errors.surname ? 'is-invalid' : ''}`}
                        id="name"
                        placeholder="Enter your name"
                        value={name}
                        onChange={(e) => {
                          setName(e.target.value)
                          if (errors.name) setErrors((prev) => ({...prev, name: undefined}))
                        }}
                        disabled={isLoading}
                        aria-invalid={Boolean(errors.name)}
                        aria-describedby={errors.name ? 'nameError' : undefined}
                    />
                  </div>
                  <div className="mb-3">
                    <label htmlFor="surname" className="form-label fw-semibold">
                      Surname
                    </label>
                    <input
                        type="text"
                        className={`form-control form-control-lg ${errors.surname ? 'is-invalid' : ''}`}
                        id="surname"
                        placeholder="Enter your surname"
                        value={surname}
                        onChange={(e) => {
                          setSurname(e.target.value)
                          if (errors.surname) setErrors((prev) => ({...prev, surname: undefined}))
                        }}
                        disabled={isLoading}
                        aria-invalid={Boolean(errors.surname)}
                        aria-describedby={errors.surname ? 'nameError' : undefined}
                    />
                  </div>
                  <div className="mb-3">
                    <label htmlFor="email" className="form-label fw-semibold">
                      Email
                    </label>
                    <input
                        type="email"
                        className={`form-control form-control-lg ${errors.email ? 'is-invalid' : ''}`}
                        id="email"
                        placeholder="Enter your email"
                        value={name}
                        onChange={(e) => {
                          setEmail(e.target.value)
                          if (errors.email) setErrors((prev) => ({...prev, email: undefined}))
                        }}
                        disabled={isLoading}
                        aria-invalid={Boolean(errors.name)}
                        aria-describedby={errors.name ? 'nameError' : undefined}
                    />
                  </div>
                  <div className="mb-3">
                    <label htmlFor="birthDate" className="form-label fw-semibold">
                      Birth date
                    </label>
                    <input
                        type="date"
                        className={`form-control form-control-lg ${errors.birthDate ? 'is-invalid' : ''}`}
                        id="bithDate"
                        placeholder="Enter your birth date"
                        value={name}
                        onChange={(e) => {
                          setBirthDate(e.target.value)
                          if (errors.birthDate) setErrors((prev) => ({...prev, birthDate: undefined}))
                        }}
                        disabled={isLoading}
                        aria-invalid={Boolean(errors.email)}
                        aria-describedby={errors.email ? 'nameError' : undefined}
                    />
                  </div>
                  <div className="mb-3">
                    <label htmlFor="login" className="form-label fw-semibold">
                      Login
                    </label>
                    <input
                        type="text"
                        className={`form-control form-control-lg ${errors.login ? 'is-invalid' : ''}`}
                        id="login"
                        placeholder="Enter your login"
                        value={login}
                        onChange={(e) => {
                          setLogin(e.target.value)
                          if (errors.login) setErrors((prev) => ({...prev, login: undefined}))
                        }}
                        disabled={isLoading}
                        aria-invalid={Boolean(errors.login)}
                        aria-describedby={errors.login ? 'loginError' : undefined}
                    />
                    {errors.login ? (
                        <div id="loginError" className="invalid-feedback">
                          {errors.login}
                        </div>
                    ) : null}
                  </div>
                  <div className="mb-3">
                    <label htmlFor="password" className="form-label fw-semibold">
                      Password
                    </label>
                    <input
                        type="password"
                        className={`form-control form-control-lg ${errors.password ? 'is-invalid' : ''}`}
                        id="password"
                        placeholder="Enter your password"
                        value={password}
                        onChange={(e) => {
                          setPassword(e.target.value)
                          if (errors.password) setErrors((prev) => ({...prev, password: undefined}))
                        }}
                        disabled={isLoading}
                        autoComplete="current-password"
                        aria-invalid={Boolean(errors.password)}
                        aria-describedby={errors.password ? 'passwordError' : undefined}
                    />
                    {errors.password ? (
                        <div id="passwordError" className="invalid-feedback">
                          {errors.password}
                        </div>
                    ) : null}
                  </div>

                  <div className="mb-3">
                    <label htmlFor="confirmedPassword" className="form-label fw-semibold">
                      Password
                    </label>
                    <input
                        type="password"
                        className={`form-control form-control-lg ${errors.confirmedPassword ? 'is-invalid' : ''}`}
                        id="confi"
                        placeholder="Confirm your password"
                        value={confirmedPassword}
                        onChange={(e) => {
                          setConfirmedPassword(e.target.value)
                          if (errors.password) setErrors((prev) => ({...prev, password: undefined}))
                        }}
                        disabled={isLoading}
                        autoComplete="current-password"
                        aria-invalid={Boolean(errors.password)}
                        aria-describedby={errors.password ? 'passwordError' : undefined}
                    />
                    {errors.password ? (
                        <div id="passwordError" className="invalid-feedback">
                          {errors.password}
                        </div>
                    ) : null}
                  </div>

                  <button
                      type="submit"
                      className="btn btn-primary btn-lg w-100 mb-3"
                      disabled={isSubmitDisabled}
                  >
                    {isLoading ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2"></span>{' '}
                          Signing up...
                        </>
                    ) : (
                        'Sign In'
                    )}
                  </button>

                  <div className="text-center">
                    <p className="text-muted small mb-0">
                      Already have an account?{' '}
                      <a href="/login" className="text-decoration-none fw-semibold">
                        Sign in
                      </a>
                    </p>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
  )
}
