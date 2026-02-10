import { useNavigate } from 'react-router-dom';
import type { LeagueSummary } from '../types';

interface Props {
  league: LeagueSummary;
}

export default function LeagueCard({ league }: Props) {
  const navigate = useNavigate();

  return (
    <div className="card league-card" onClick={() => navigate(`/leagues/${league.leagueKey}`)}>
      <h3>{league.name}</h3>
      <div className="meta">
        <span>{league.season}</span>
        <span>{league.scoringType}</span>
        <span>{league.numTeams} teams</span>
        <span>Week {league.currentWeek}</span>
      </div>
    </div>
  );
}
