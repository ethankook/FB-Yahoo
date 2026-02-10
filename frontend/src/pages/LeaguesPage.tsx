import { useState, useEffect, useCallback } from 'react';
import { apiFetch } from '../api/client';
import type { LeagueSummary } from '../types';
import LeagueCard from '../components/LeagueCard';
import SyncButton from '../components/SyncButton';

export default function LeaguesPage() {
  const [leagues, setLeagues] = useState<LeagueSummary[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchLeagues = useCallback(async () => {
    setLoading(true);
    try {
      const data = await apiFetch<LeagueSummary[]>('/api/leagues');
      setLeagues(data);
    } catch (e) {
      console.error('Failed to fetch leagues:', e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchLeagues();
  }, [fetchLeagues]);

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Leagues</h1>
        <SyncButton onComplete={fetchLeagues} />
      </div>
      {loading ? (
        <p style={{ color: 'var(--text-muted)' }}>Loading leagues...</p>
      ) : leagues.length === 0 ? (
        <p style={{ color: 'var(--text-muted)' }}>No leagues found. Click Sync to pull data from Yahoo.</p>
      ) : (
        <div className="league-grid">
          {leagues.map(league => (
            <LeagueCard key={league.leagueKey} league={league} />
          ))}
        </div>
      )}
    </div>
  );
}
