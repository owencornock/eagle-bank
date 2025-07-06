import React, { useState, useEffect } from 'react';
import api from '../api/axiosClient';

export default function AccountsList({ onSelectAccount, refreshTrigger }) {
    const [accounts, setAccounts] = useState([]);
    const [isCreating, setIsCreating] = useState(false);
    const [newAccount, setNewAccount] = useState({
        name: '',
        type: 'SAVINGS',
        currency: 'GBP'
    });

    useEffect(() => {
        loadAccounts();
    }, [refreshTrigger]);

    const loadAccounts = async () => {
        try {
            const response = await api.get('/v1/accounts');
            setAccounts(response.data);
        } catch (error) {
            console.error('Failed to load accounts:', error);
        }
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        try {
            await api.post('/v1/accounts', newAccount);
            setIsCreating(false);
            setNewAccount({ name: '', type: 'SAVINGS', currency: 'GBP' });
            loadAccounts();
        } catch (error) {
            console.error('Failed to create account:', error);
            alert(error.response?.data?.error || 'Failed to create account');
        }
    };

    return (
        <div className="bg-white p-4 rounded-lg shadow">
            <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold">Accounts</h2>
                <button
                    onClick={() => setIsCreating(true)}
                    className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                >
                    New Account
                </button>
            </div>

            {isCreating && (
                <form onSubmit={handleCreate} className="mb-4 space-y-2">
                    <input
                        type="text"
                        value={newAccount.name}
                        onChange={e => setNewAccount({...newAccount, name: e.target.value})}
                        placeholder="Account Name"
                        className="w-full p-2 border rounded"
                        required
                    />
                    <select
                        value={newAccount.type}
                        onChange={e => setNewAccount({...newAccount, type: e.target.value})}
                        className="w-full p-2 border rounded"
                        required
                    >
                        <option value="SAVINGS">Savings</option>
                        <option value="CHECKING">Checking</option>
                    </select>
                    <select
                        value={newAccount.currency}
                        onChange={e => setNewAccount({...newAccount, currency: e.target.value})}
                        className="w-full p-2 border rounded"
                        required
                    >
                        <option value="GBP">GBP</option>
                        <option value="USD">USD</option>
                        <option value="EUR">EUR</option>
                    </select>
                    <div className="flex space-x-2">
                        <button type="submit" className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700">
                            Create
                        </button>
                        <button
                            type="button"
                            onClick={() => setIsCreating(false)}
                            className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            )}

            <div className="space-y-2">
                {accounts.map(account => (
                    <div
                        key={account.id}
                        onClick={() => onSelectAccount(account.id)}
                        className="p-2 border rounded cursor-pointer hover:bg-gray-50"
                    >
                        <div className="font-bold">{account.name}</div>
                        <div className="text-gray-600">
                            Balance: {account.balance} {account.currency}
                        </div>
                        <div className="text-gray-500 text-sm">
                            Type: {account.type}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}