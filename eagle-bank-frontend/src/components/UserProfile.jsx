import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../auth/AuthContext';
import api from '../api/axiosClient';

function getUserIdFromToken(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.sub;
    } catch (e) {
        console.error('Failed to decode token:', e);
        return null;
    }
}

export default function UserProfile() {
    const { token, logout } = useContext(AuthContext);
    const [user, setUser] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [formData, setFormData] = useState({});
    const [error, setError] = useState(null);

    useEffect(() => {
        loadUser();
    }, []);

    const loadUser = async () => {
        try {
            const userId = getUserIdFromToken(token);
            if (!userId) {
                throw new Error('Invalid token');
            }
            const response = await api.get(`/v1/users/${userId}`);
            setUser(response.data);
            setFormData(response.data);
            setError(null);
        } catch (error) {
            console.error('Failed to load user:', error);
            setError('Failed to load user profile');
        }
    };


    const handleUpdate = async (e) => {
        e.preventDefault();
        try {
            const userId = getUserIdFromToken(token);
            if (!userId) {
                throw new Error('Invalid token');
            }

            // Create an object with only the changed fields
            const changedFields = {};
            if (formData.firstName !== user.firstName) changedFields.firstName = formData.firstName;
            if (formData.lastName !== user.lastName) changedFields.lastName = formData.lastName;
            if (formData.email !== user.email) changedFields.email = formData.email;
            if (formData.dob !== user.dob) changedFields.dob = formData.dob;

            // Only send the request if there are changes
            if (Object.keys(changedFields).length > 0) {
                await api.patch(`/v1/users/${userId}`, changedFields);
                setIsEditing(false);
                loadUser();
                setError(null);
            } else {
                setIsEditing(false); // No changes to save
            }
        } catch (error) {
            console.error('Failed to update user:', error);
            setError(error.response?.data?.error || 'Failed to update profile');
        }
    };

    const handleDelete = async () => {
        if (window.confirm('Are you sure you want to delete your account? This cannot be undone.')) {
            try {
                const userId = getUserIdFromToken(token);
                if (!userId) {
                    throw new Error('Invalid token');
                }
                await api.delete(`/v1/users/${userId}`);
                logout();
            } catch (error) {
                console.error('Failed to delete user:', error);
                setError('Failed to delete account');
            }
        }
    };

    if (!user && !error) return <div>Loading...</div>;

    return (
        <div className="bg-white p-4 rounded-lg shadow">
            <h2 className="text-xl font-bold mb-4">Profile</h2>
            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4">
                    {error}
                </div>
            )}
            {isEditing ? (
                <form onSubmit={handleUpdate} className="space-y-2">
                    <input
                        type="text"
                        value={formData.firstName}
                        onChange={e => setFormData({...formData, firstName: e.target.value})}
                        className="w-full p-2 border rounded"
                        placeholder="First Name"
                    />
                    <input
                        type="text"
                        value={formData.lastName}
                        onChange={e => setFormData({...formData, lastName: e.target.value})}
                        className="w-full p-2 border rounded"
                        placeholder="Last Name"
                    />
                    <input
                        type="email"
                        value={formData.email}
                        onChange={e => setFormData({...formData, email: e.target.value})}
                        className="w-full p-2 border rounded"
                        placeholder="Email"
                    />
                    <input
                        type="date"
                        value={formData.dob}
                        onChange={e => setFormData({...formData, dob: e.target.value})}
                        className="w-full p-2 border rounded"
                    />
                    <div className="flex space-x-2">
                        <button type="submit" className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700">
                            Save
                        </button>
                        <button
                            type="button"
                            onClick={() => setIsEditing(false)}
                            className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700">
                            Cancel
                        </button>
                    </div>
                </form>
            ) : (
                user && (
                    <div className="space-y-2">
                        <p><strong>Name:</strong> {user.firstName} {user.lastName}</p>
                        <p><strong>Email:</strong> {user.email}</p>
                        <p><strong>DOB:</strong> {new Date(user.dob).toLocaleDateString()}</p>
                        <div className="flex space-x-2">
                            <button
                                onClick={() => setIsEditing(true)}
                                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
                                Edit
                            </button>
                            <button
                                onClick={handleDelete}
                                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700">
                                Delete Account
                            </button>
                        </div>
                    </div>
                )
            )}
        </div>
    );
}