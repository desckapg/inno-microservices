import {type FormEvent, useMemo, useState} from 'react'
import {login as loginRequest} from '../api/services/auth-service'
import {tokenStore} from '../utils/token-store'
import {useNavigate} from 'react-router'
import {AuthFormField} from '../components/AuthFormField'

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
  const navigate = useNavigate()
  const [login, setLogin] = useState('')
  const [password, setPassword] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const [errors, setErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)

  const isSubmitDisabled = useMemo(() => {
    return isLoading
  }, [isLoading])

  const clearFieldError = (field: keyof FieldErrors) => {
    setErrors((prev) => ({...prev, [field]: undefined}))
  }

  const performLogin = async () => {
    const response = await loginRequest({
      login: login.trim(),
      password,
    })
    tokenStore.setTokens(response.accessToken, response.refreshToken)
    navigate('/orders')
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setFormError(null)

    const nextErrors = validate({login, password})
    setErrors(nextErrors)

    if (Object.keys(nextErrors).length > 0) return

    setIsLoading(true)
    try {
      await performLogin()
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
                      autoComplete="current-password"
                      onChange={(e) => {
                        setPassword(e.target.value)
                        clearFieldError('password')
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
