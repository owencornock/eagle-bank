import React, { useState, useContext, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { AuthContext } from '../auth/AuthContext';
import UserProfile from '../components/UserProfile';
import AccountsList from '../components/AccountsList';
import AccountDetails from '../components/AccountDetails';
import api from '../api/axiosClient';

export default function Dashboard() {
    const [selectedAccount, setSelectedAccount] = useState(null);
    const [refreshTrigger, setRefreshTrigger] = useState(0);
    const { logout } = useContext(AuthContext);
    const location = useLocation();

    const handleAccountDeleted = () => {
        setSelectedAccount(null);
        setRefreshTrigger(prev => prev + 1);
    };

    useEffect(() => {
        if (location.state?.refresh) {
            handleAccountDeleted();
            window.history.replaceState({}, document.title);
        }
    }, [location]);

    return (
        <div className="container mx-auto p-4">
            <div className="mb-4 flex justify-end">
                <button
                    onClick={logout}
                    className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                >
                    Logout
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-12 gap-4">
                <div className="md:col-span-3 space-y-4">
                    <UserProfile />
                    <AccountsList
                        onSelectAccount={setSelectedAccount}
                        refreshTrigger={refreshTrigger}
                    />
                </div>

                <div className="md:col-span-9">
                    {selectedAccount ? (
                        <AccountDetails
                            accountId={selectedAccount}
                            onAccountDeleted={handleAccountDeleted}
                            setSelectedAccount={setSelectedAccount}
                        />
                    ) : (
                        <div className="text-center text-gray-500 mt-10">
                            Select an account to view details
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}