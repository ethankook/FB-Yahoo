import type { InsightsDto } from '../types';

interface Props {
  insights: InsightsDto | null;
}

const CAT_LABELS: Record<string, string> = {
  pts: 'Points',
  reb: 'Rebounds',
  ast: 'Assists',
  stl: 'Steals',
  blk: 'Blocks',
  tov: 'Turnovers',
  fgPct: 'FG%',
  ftPct: 'FT%',
  fg3: '3PM',
};

export default function InsightsPanel({ insights }: Props) {
  if (!insights) {
    return (
      <div className="card panel">
        <h2>Team Insights</h2>
        <p style={{ color: 'var(--text-muted)' }}>No insights available</p>
      </div>
    );
  }

  return (
    <div className="card panel">
      <h2>Team Insights</h2>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
        <div>
          <h3 style={{ fontSize: '0.875rem', color: 'var(--green)', marginBottom: '0.75rem' }}>Strongest Categories</h3>
          {insights.strongest.map(cat => (
            <div key={cat.category} className="insight-item">
              <div>
                <div style={{ fontWeight: 500 }}>{CAT_LABELS[cat.category] ?? cat.category}</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  Avg: {cat.myValue.toFixed(2)} (League: {cat.leagueAvg.toFixed(2)})
                </div>
              </div>
              <span className="rank strong">#{cat.rank}/{cat.totalTeams}</span>
            </div>
          ))}
        </div>
        <div>
          <h3 style={{ fontSize: '0.875rem', color: 'var(--red)', marginBottom: '0.75rem' }}>Weakest Categories</h3>
          {insights.weakest.map(cat => (
            <div key={cat.category} className="insight-item">
              <div>
                <div style={{ fontWeight: 500 }}>{CAT_LABELS[cat.category] ?? cat.category}</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  Avg: {cat.myValue.toFixed(2)} (League: {cat.leagueAvg.toFixed(2)})
                </div>
              </div>
              <span className="rank weak">#{cat.rank}/{cat.totalTeams}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
