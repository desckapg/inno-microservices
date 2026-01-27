import {type FormEvent, useMemo, useState} from 'react'
import {login as loginRequest} from '../api/services/auth-service'

type FieldErrors = {
  login?: string
  password?: string
}

function validate(values: { login: string; password: string }): FieldErrors {
  const errors: FieldErrors = {}

  const login = values.login.trim()
  const password = values.password

  if (!login) errors.login = 'Login is required.'
  else if (login.length < 3) errors.login = 'Login must be at least 3 characters.'

  if (!password) errors.password = 'Password is required.'

  return errors
}

function getErrorMessage(err: unknown): string {
  if (err instanceof Error) return err.message
  return 'Login failed. Please check your credentials and try again.'
}

export function LoginPage() {
  const [login, setLogin] = useState('')
  const [password, setPassword] = useState('')
  const [rememberMe, setRememberMe] = useState(false)
  const [isLoading, setIsLoading] = useState(false)

  const [errors, setErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)

  const isSubmitDisabled = useMemo(() => {
    return isLoading
  }, [isLoading])

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setFormError(null)

    const nextErrors = validate({login, password})
    setErrors(nextErrors)

    if (Object.keys(nextErrors).length > 0) return

    setIsLoading(true)
    try {
      const response = await loginRequest({
        login: login.trim(),
        password,
      })

      console.log('Login success:', response)
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

                  <div className="d-flex justify-content-between align-items-center mb-4">
                    <div className="form-check">
                      <input
                          type="checkbox"
                          className="form-check-input"
                          id="rememberMe"
                          checked={rememberMe}
                          onChange={(e) => setRememberMe(e.target.checked)}
                          disabled={isLoading}
                      />
                      <label className="form-check-label" htmlFor="rememberMe">
                        Remember me
                      </label>
                    </div>
                  </div>

                  <button
                      type="submit"
                      className="btn btn-primary btn-lg w-100 mb-3"
                      disabled={isSubmitDisabled}
                  >
                    {isLoading ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2"></span>{' '}
                          Signing in...
                        </>
                    ) : (
                        'Sign In'
                    )}
                  </button>

                  <div className="text-center">
                    <p className="text-muted small mb-0">
                      Don&apos;t have an account?{' '}
                      <a href="/register" className="text-decoration-none fw-semibold">
                        Sign up
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
