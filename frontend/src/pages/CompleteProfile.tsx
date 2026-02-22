import { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { useNavigate } from 'react-router';
import axios from 'axios';
import {createProfile} from "../api/services/user-service.ts";

export function CompleteProfile() {
    const auth = useAuth();
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        birthDate: ''
    });
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!auth.isLoading && !auth.isAuthenticated) {
            auth.signinRedirect();
        }
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [auth.isLoading, auth.isAuthenticated]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!auth.user?.access_token) {
            setError("User is not authenticated");
            return;
        }

        setSubmitting(true);
        setError('');

        try {
             await createProfile({
                 id: auth.user.profile.sub,
                 name: formData.firstName,
                 surname: formData.lastName,
                 birthDate: formData.birthDate,
                 email: auth.user.profile.email as string
             });

             // Redirect to dashboard (orders page based on existing app)
             navigate('/orders');
        } catch (err: unknown) {
             console.error("Profile creation failed", err);
             if (axios.isAxiosError(err)) {
                 setError(err.response?.data?.message || 'Failed to complete profile. Please try again.');
             } else {
                 setError('An unexpected error occurred.');
             }
             setSubmitting(false);
        }
    };

    if (auth.isLoading) {
        return <div>Loading...</div>;
    }

    if (!auth.isAuthenticated) {
        return <div>Redirecting to login...</div>;
    }

    return (
        <div className="container mt-5" style={{ maxWidth: '500px' }}>
            <div className="card shadow-sm">
                <div className="card-body">
                    <h2 className="card-title mb-4">Complete Your Profile</h2>
                    <p className="text-muted mb-4">Please provide additional details to finish setting up your account.</p>

                    {error && <div className="alert alert-danger">{error}</div>}

                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label htmlFor="firstName" className="form-label">First Name</label>
                            <input
                                id="firstName"
                                type="text"
                                className="form-control"
                                value={formData.firstName}
                                onChange={e => setFormData({...formData, firstName: e.target.value})}
                                required
                            />
                        </div>
                        <div className="mb-3">
                            <label htmlFor="lastName" className="form-label">Last Name</label>
                            <input
                                id="lastName"
                                type="text"
                                className="form-control"
                                value={formData.lastName}
                                onChange={e => setFormData({...formData, lastName: e.target.value})}
                                required
                            />
                        </div>
                        <div className="mb-3">
                            <label htmlFor="birthDate" className="form-label">Birth Date</label>
                            <input
                                id="birthDate"
                                type="date"
                                className="form-control"
                                value={formData.birthDate}
                                onChange={e => setFormData({...formData, birthDate: e.target.value})}
                                required
                            />
                        </div>
                        <button type="submit" className="btn btn-primary w-100" disabled={submitting}>
                            {submitting ? 'Saving...' : 'Save & Continue'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}

