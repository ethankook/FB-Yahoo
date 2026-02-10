import type { LeagueSummary } from '../types';

interface Props {
  league: LeagueSummary;
}

export default function LeagueHeader({ league }: Props) {
  return (
    <div className="card panel">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', marginBottom: '0.25rem' }}>{league.name}</h1>
          <div className="meta" style={{ color: 'var(--text-muted)', display: 'flex', gap: '1rem', fontSize: '0.875rem' }}>
            <span>{league.season} Season</span>
            <span>{league.scoringType}</span>
            <span>Week {league.currentWeek}</span>
            <span>{league.numTeams} Teams</span>
          </div>
        </div>
      </div>
    </div>
  );
}
