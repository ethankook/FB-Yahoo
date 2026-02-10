import type { MatchupDto } from '../types';

interface Props {
  matchup: MatchupDto | null;
}

export default function MatchupPanel({ matchup }: Props) {
  if (!matchup || !matchup.opponent) {
    return (
      <div className="card panel">
        <h2>Current Matchup</h2>
        <p style={{ color: 'var(--text-muted)' }}>No matchup data available</p>
      </div>
    );
  }

  let summaryText: string;
  let summaryClass: string;

  if (matchup.myWins > matchup.opponentWins) {
    summaryText = `Winning ${matchup.myWins}-${matchup.opponentWins}`;
    summaryClass = 'winning';
  } else if (matchup.myWins < matchup.opponentWins) {
    summaryText = `Losing ${matchup.myWins}-${matchup.opponentWins}`;
    summaryClass = 'losing';
  } else {
    summaryText = `Tied ${matchup.myWins}-${matchup.opponentWins}`;
    summaryClass = 'tied';
  }
  if (matchup.ties > 0) {
    summaryText += ` (${matchup.ties} tied)`;
  }

  return (
    <div className="card panel">
      <h2>Week {matchup.week} vs {matchup.opponent.name}</h2>
      <div className={`matchup-summary ${summaryClass}`}>{summaryText}</div>
      <div>
        {matchup.stats.map((stat, i) => (
          <div key={i} className="stat-row">
            <div className={`value left ${stat.winner === 'me' ? 'winner' : stat.winner === 'opponent' ? 'loser' : ''}`}>
              {stat.myValue ?? '-'}
            </div>
            <div className="label">{stat.statName}</div>
            <div className={`value right ${stat.winner === 'opponent' ? 'winner' : stat.winner === 'me' ? 'loser' : ''}`}>
              {stat.opponentValue ?? '-'}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
