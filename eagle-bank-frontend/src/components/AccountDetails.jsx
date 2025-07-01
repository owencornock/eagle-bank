
import React, { useState, useEffect } from 'react';
import api from '../api/axiosClient';
import { useNavigate } from 'react-router-dom';

export default function AccountDetails({ accountId, onAccountDeleted }) {
    const [account, setAccount] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [isEditing, setIsEditing] = useState(false);
    const [newName, setNewName] = useState('');
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const [isCreatingTransaction, setIsCreatingTransaction] = useState(false);
    const [transactionForm, setTransactionForm] = useState({
        type: 'DEPOSIT',
        amount: ''
    });

    useEffect(() => {
        if (accountId) {
            loadAccount();
            loadTransactions();
        }
    }, [accountId]);

    const loadAccount = async () => {
        try {
            const response = await api.get(`/v1/accounts/${accountId}`);
            setAccount(response.data);
            setNewName(response.data.name);
        } catch (error) {
            console.error('Failed to load account:', error);
        }
    };

    const loadTransactions = async () => {
        try {
            const response = await api.get(`/v1/accounts/${accountId}/transactions`);
            setTransactions(response.data);
        } catch (error) {
            console.error('Failed to load transactions:', error);
        }
    };

    const handleUpdateName = async (e) => {
        e.preventDefault();
        try {
            const response = await api.patch(`/v1/accounts/${accountId}`, { name: newName });
            setIsEditing(false);
            await loadAccount();
        } catch (error) {
            console.error('Failed to update account:', error);
            console.error('Error response:', error.response?.data);
            alert('Failed to update account name: ' + (error.response?.data?.message || error.message));
        }
    };

    const handleCreateTransaction = async (e) => {
        e.preventDefault();
        try {
            await api.post(`/v1/accounts/${accountId}/transactions`, transactionForm);
            setIsCreatingTransaction(false);
            setTransactionForm({ type: 'DEPOSIT', amount: '' });
            loadAccount();
            loadTransactions();
        } catch (error) {
            console.error('Failed to create transaction:', error);
        }
    };

    const handleDeleteAccount = async () => {
        if (window.confirm('Are you sure you want to delete this account? This cannot be undone.')) {
            try {
                await api.delete(`/v1/accounts/${accountId}`);
                if (onAccountDeleted) {
                    onAccountDeleted();
                }
            } catch (error) {
                const errorMessage = error.response?.data?.error || 'Failed to delete account';
                console.error('Failed to delete account:', error);
                setError(errorMessage);
            }
        }
    };

    if (!account) return <div>Loading...</div>;

    return (
        <div className="space-y-4">
            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative">
                    {error}
                </div>
            )}
            <div className="bg-white p-4 rounded-lg shadow">
                <div className="flex justify-between items-center mb-4">
                    {isEditing ? (
                        <form onSubmit={handleUpdateName} className="flex space-x-2">
                            <input
                                type="text"
                                value={newName}
                                onChange={e => setNewName(e.target.value)}
                                className="p-2 border rounded"
                                required
                                minLength="1"
                            />
                            <button
                                type="submit"
                                className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                            >
                                Save
                            </button>
                            <button
                                type="button"
                                onClick={() => {
                                    setIsEditing(false);
                                    setNewName(account.name);
                                }}
                                className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
                            >
                                Cancel
                            </button>
                        </form>
                    ) : (
                        <>
                            <h2 className="text-xl font-bold">{account.name}</h2>
                            <div className="space-x-2">
                                <button
                                    onClick={() => setIsEditing(true)}
                                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                                >
                                    Edit Name
                                </button>
                                <button
                                    onClick={handleDeleteAccount}
                                    className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                                >
                                    Delete Account
                                </button>
                            </div>
                        </>
                    )}
                </div>
                <div className="text-2xl font-bold">Balance: ${account.balance}</div>
            </div>

            <div className="bg-white p-4 rounded-lg shadow">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-lg font-bold">Transactions</h3>
                    <button
                        onClick={() => setIsCreatingTransaction(true)}
                        className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                    >
                        New Transaction
                    </button>
                </div>

                {isCreatingTransaction && (
                    <form onSubmit={handleCreateTransaction} className="mb-4 space-y-2">
                        <select
                            value={transactionForm.type}
                            onChange={e => setTransactionForm({...transactionForm, type: e.target.value})}
                            className="w-full p-2 border rounded"
                        >
                            <option value="DEPOSIT">Deposit</option>
                            <option value="WITHDRAWAL">Withdrawal</option>
                        </select>
                        <input
                            type="number"
                            step="0.01"
                            value={transactionForm.amount}
                            onChange={e => setTransactionForm({...transactionForm, amount: e.target.value})}
                            placeholder="Amount"
                            className="w-full p-2 border rounded"
                            required
                        />
                        <div className="flex space-x-2">
                            <button type="submit" className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700">Create</button>
                            <button
                                type="button"
                                onClick={() => setIsCreatingTransaction(false)}
                                className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
                            >
                                Cancel
                            </button>
                        </div>
                    </form>
                )}

                <div className="space-y-2">
                    {transactions.map(transaction => (
                        <div key={transaction.id} className="p-2 border rounded">
                            <div className="flex justify-between">
                                <span className={transaction.type === 'DEPOSIT' ? 'text-green-600' : 'text-red-600'}>
                                    {transaction.type === 'DEPOSIT' ? '+' : '-'}${transaction.amount}
                                </span>
                                <span className="text-gray-500">
                                    {new Date(transaction.timestamp).toLocaleString()}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}