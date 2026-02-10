import { useState, useEffect } from 'react';
import { apiFetch } from '../api/client';
import type { AvailablePlayer } from '../types';

interface Props {
  leagueKey: string;
}

const CATEGORIES = [
  { key: 'pts', label: 'PTS' },
  { key: 'reb', label: 'REB' },
  { key: 'ast', label: 'AST' },
  { key: 'stl', label: 'STL' },
  { key: 'blk', label: 'BLK' },
  { key: 'tov', label: 'TOV' },
  { key: 'fg_pct', label: 'FG%' },
  { key: 'ft_pct', label: 'FT%' },
  { key: 'fg3', label: '3PM' },
];

const STAT_GETTER: Record<string, (p: AvailablePlayer) => number | null> = {
  pts: p => p.stats?.ptsPg ?? null,
  reb: p => p.stats?.rebPg ?? null,
  ast: p => p.stats?.astPg ?? null,
  stl: p => p.stats?.stlPg ?? null,
  blk: p => p.stats?.blkPg ?? null,
  tov: p => p.stats?.tovPg ?? null,
  fg_pct: p => p.stats?.fgPct ?? null,
  ft_pct: p => p.stats?.ftPct ?? null,
  fg3: p => p.stats?.fg3MadePg ?? null,
};

export default function RecommendationsPanel({ leagueKey }: Props) {
  const [category, setCategory] = useState('pts');
  const [players, setPlayers] = useState<AvailablePlayer[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    apiFetch<AvailablePlayer[]>(`/api/leagues/${leagueKey}/available?category=${category}&limit=10`)
      .then(setPlayers)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [leagueKey, category]);

  const catLabel = CATEGORIES.find(c => c.key === category)?.label ?? category;
  const getStatValue = STAT_GETTER[category] ?? (() => null);

  return (
    <div className="card panel">
      <h2>Top Available Players</h2>
      <div className="category-toggles">
        {CATEGORIES.map(c => (
          <button
            key={c.key}
            className={category === c.key ? 'active' : ''}
            onClick={() => setCategory(c.key)}
          >
            {c.label}
          </button>
        ))}
      </div>
      {loading ? (
        <p style={{ color: 'var(--text-muted)' }}>Loading...</p>
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Player</th>
                <th>Pos</th>
                <th>Team</th>
                <th>{catLabel}</th>
                <th>Own%</th>
              </tr>
            </thead>
            <tbody>
              {players.map((p, i) => {
                const val = getStatValue(p);
                return (
                  <tr key={p.playerId}>
                    <td>{i + 1}</td>
                    <td>{p.name}</td>
                    <td>{p.displayPosition}</td>
                    <td>{p.editorialTeamAbbr}</td>
                    <td>{val != null ? (category.includes('pct') ? (val * 100).toFixed(1) + '%' : val.toFixed(1)) : '-'}</td>
                    <td>{p.percentOwned != null ? p.percentOwned + '%' : '-'}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
