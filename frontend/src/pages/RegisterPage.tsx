import {type FormEvent, useMemo, useState} from 'react'
import {register as registerRequest} from '../api/services/auth-service'
import {tokenStore} from '../utils/token-store'
import {useNavigate} from 'react-router'
import {AuthFormField} from '../components/AuthFormField'

type FieldErrors = {
  name?: string
  surname?: string
  email?: string
  birthDate?: string
  login?: string
  password?: string
  confirmedPassword?: string
}

function validate(values: { name: string,
                    surname: string,
                    email: string,
                    birthDate: string,
                    login: string;
                    password: string,
                    confirmedPassword: string }): FieldErrors {
  const errors: FieldErrors = {}

  const name = values.name.trim()
  const surname = values.surname.trim()
  const email = values.email
  const birthDate = Date.parse(values.birthDate)
  const login = values.login.trim()
  const password = values.password
  const confirmedPassword = values.confirmedPassword

  if (!name) errors.name = 'Name is required'

  if (!surname) errors.surname = 'Surname is required'

  if (!email) errors.email = 'Email is required'

  if (!birthDate) errors.birthDate = 'Birth date is required'

  if (!login) errors.login = 'Login is required.'
  else if (login.length < 3) errors.login = 'Login must be at least 3 characters.'

  if (!password) errors.password = 'Password is required.'

  if (!confirmedPassword) errors.confirmedPassword = 'Confirmed password is required'
  else if (password !== confirmedPassword) errors.confirmedPassword = 'Passwords do not match'

  return errors
}

function getErrorMessage(err: unknown): string {
  if (err instanceof Error) return err.message
  return 'Registration failed. Please check your information and try again.'
}

export function RegisterPage() {
  const navigate = useNavigate()
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

  const clearFieldError = (field: keyof FieldErrors) => {
    setErrors((prev) => ({...prev, [field]: undefined}))
  }

  const performRegistration = async () => {
    const response = await registerRequest({
      name: name.trim(),
      surname: surname.trim(),
      email: email.trim(),
      birthDate,
      login: login.trim(),
      password,
    })
    tokenStore.setTokens(response.accessToken, response.refreshToken)
    navigate('/orders')
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setFormError(null)

    const nextErrors = validate({name, surname, email, birthDate, login, password, confirmedPassword})
    setErrors(nextErrors)

    if (Object.keys(nextErrors).length > 0) return

    setIsLoading(true)
    try {
      await performRegistration()
    } catch (err) {
      setFormError(getErrorMessage(err))
    } finally {
      setIsLoading(false)
    }
  }

  return (
      <div className="container">
        <div className="row justify-content-center align-items-center min-vh-100">
          <div className="col-12 col-sm-10 col-md-10 col-lg-8 col-xl-7">
            <div className="card shadow border-0">
              <div className="card-body p-4 p-md-5">
                <div className="text-center mb-4">
                  <h1 className="h3 fw-bold mb-3">Create Account</h1>
                  <p className="text-muted">Sign up for a new account</p>
                </div>

                {formError ? (
                    <div className="alert alert-danger" role="alert">
                      {formError}
                    </div>
                ) : null}

                <form onSubmit={handleSubmit} noValidate>
                  <div className="row mb-3">
                    <div className="col-sm-6">
                      <label htmlFor="name" className="form-label fw-semibold">
                        Name
                      </label>
                      <input
                          type="text"
                          className={`form-control form-control-lg ${errors.name ? 'is-invalid' : ''}`}
                          id="name"
                          placeholder="Enter your name"
                          value={name}
                          onChange={(e) => {
                            setName(e.target.value)
                            clearFieldError('name')
                          }}
                          disabled={isLoading}
                          aria-invalid={Boolean(errors.name)}
                          aria-describedby={errors.name ? 'nameError' : undefined}
                      />
                      {errors.name ? (
                          <div id="nameError" className="invalid-feedback">
                            {errors.name}
                          </div>
                      ) : null}
                    </div>
                    <div className="col-sm-6">
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
                            clearFieldError('surname')
                          }}
                          disabled={isLoading}
                          aria-invalid={Boolean(errors.surname)}
                          aria-describedby={errors.surname ? 'surnameError' : undefined}
                      />
                      {errors.surname ? (
                          <div id="surnameError" className="invalid-feedback">
                            {errors.surname}
                          </div>
                      ) : null}
                    </div>
                  </div>

                  <AuthFormField
                      id="email"
                      label="Email"
                      type="email"
                      placeholder="Enter your email"
                      value={email}
                      error={errors.email}
                      disabled={isLoading}
                      onChange={(e) => {
                        setEmail(e.target.value)
                        clearFieldError('email')
                      }}
                  />

                  <AuthFormField
                      id="birthDate"
                      label="Birth date"
                      type="date"
                      placeholder="Enter your birth date"
                      value={birthDate}
                      error={errors.birthDate}
                      disabled={isLoading}
                      onChange={(e) => {
                        setBirthDate(e.target.value)
                        clearFieldError('birthDate')
                      }}
                  />

                  <AuthFormField
                      id="login"
                      label="Login"
                      type="text"
                      placeholder="Enter your login"
                      value={login}
                      error={errors.login}
                      disabled={isLoading}
                      onChange={(e) => {
                        setLogin(e.target.value)
                        clearFieldError('login')
                      }}
                  />

                  <AuthFormField
                      id="password"
                      label="Password"
                      type="password"
                      placeholder="Enter your password"
                      value={password}
                      error={errors.password}
                      disabled={isLoading}
                      autoComplete="new-password"
                      onChange={(e) => {
                        setPassword(e.target.value)
                        clearFieldError('password')
                      }}
                  />

                  <AuthFormField
                      id="confirmedPassword"
                      label="Confirm Password"
                      type="password"
                      placeholder="Confirm your password"
                      value={confirmedPassword}
                      error={errors.confirmedPassword}
                      disabled={isLoading}
                      autoComplete="new-password"
                      onChange={(e) => {
                        setConfirmedPassword(e.target.value)
                        clearFieldError('confirmedPassword')
                      }}
                  />

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
                        'Sign Up'
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
