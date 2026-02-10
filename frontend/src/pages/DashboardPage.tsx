import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { apiFetch } from '../api/client';
import type { LeagueDetail, Standing, MatchupDto, RosterPlayer, InsightsDto } from '../types';
import LeagueHeader from '../components/LeagueHeader';
import MyTeamPanel from '../components/MyTeamPanel';
import MatchupPanel from '../components/MatchupPanel';
import RosterTable from '../components/RosterTable';
import RecommendationsPanel from '../components/RecommendationsPanel';
import InsightsPanel from '../components/InsightsPanel';
import StandingsTable from '../components/StandingsTable';

export default function DashboardPage() {
  const { leagueKey } = useParams<{ leagueKey: string }>();

  const [detail, setDetail] = useState<LeagueDetail | null>(null);
  const [standings, setStandings] = useState<Standing[]>([]);
  const [matchup, setMatchup] = useState<MatchupDto | null>(null);
  const [roster, setRoster] = useState<RosterPlayer[]>([]);
  const [insights, setInsights] = useState<InsightsDto | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!leagueKey) return;

    setLoading(true);

    Promise.all([
      apiFetch<LeagueDetail>(`/api/leagues/${leagueKey}`).catch(() => null),
      apiFetch<Standing[]>(`/api/leagues/${leagueKey}/standings`).catch(() => []),
      apiFetch<MatchupDto>(`/api/leagues/${leagueKey}/matchup`).catch(() => null),
      apiFetch<RosterPlayer[]>(`/api/leagues/${leagueKey}/roster`).catch(() => []),
      apiFetch<InsightsDto>(`/api/leagues/${leagueKey}/insights`).catch(() => null),
    ]).then(([d, s, m, r, i]) => {
      setDetail(d);
      setStandings(s as Standing[]);
      setMatchup(m);
      setRoster(r as RosterPlayer[]);
      setInsights(i);
    }).finally(() => setLoading(false));
  }, [leagueKey]);

  if (loading || !detail) {
    return <div className="loading">{loading ? 'Loading dashboard...' : 'League not found'}</div>;
  }

  return (
    <div className="page">
      <Link to="/leagues" className="back-link">&larr; Back to Leagues</Link>
      <div className="dashboard-grid">
        <LeagueHeader league={detail.league} />
        <MyTeamPanel team={detail.myTeam} />
        <MatchupPanel matchup={matchup} />
        <RosterTable roster={roster} />
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.25rem' }}>
          <RecommendationsPanel leagueKey={leagueKey!} />
          <InsightsPanel insights={insights} />
        </div>
        <StandingsTable standings={standings} />
      </div>
    </div>
  );
}
