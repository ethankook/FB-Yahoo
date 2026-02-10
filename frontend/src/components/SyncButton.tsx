import { useState } from 'react';
import { apiFetch } from '../api/client';

interface Props {
  onComplete: () => void;
}

export default function SyncButton({ onComplete }: Props) {
  const [syncing, setSyncing] = useState(false);

  const handleSync = async () => {
    setSyncing(true);
    try {
      await apiFetch('/api/sync', { method: 'POST' });
      onComplete();
    } catch (e) {
      console.error('Sync failed:', e);
    } finally {
      setSyncing(false);
    }
  };

  return (
    <button className="btn-primary" onClick={handleSync} disabled={syncing}>
      {syncing ? 'Syncing...' : 'Sync Data'}
    </button>
  );
}
