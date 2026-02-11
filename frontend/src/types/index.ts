export interface LeagueSummary {
  leagueKey: string;
  name: string;
  season: number;
  scoringType: string;
  numTeams: number;
  currentWeek: number;
  matchupWeek: number;
  logoUrl: string | null;
}

export interface TeamDto {
  teamKey: string;
  name: string;
  logoUrl: string | null;
  managerNickname: string;
  waiverPriority: number;
  numberOfMoves: number;
  numberOfTrades: number;
  rank: number | null;
  wins: number | null;
  losses: number | null;
  ties: number | null;
  pct: number | null;
}

export interface LeagueDetail {
  league: LeagueSummary;
  myTeam: TeamDto | null;
}

export interface PlayerStatsDto {
  ptsPg: number | null;
  rebPg: number | null;
  astPg: number | null;
  stlPg: number | null;
  blkPg: number | null;
  tovPg: number | null;
  fgPct: number | null;
  ftPct: number | null;
  fg3MadePg: number | null;
  gamesPlayed: number | null;
}

export interface RosterPlayer {
  playerId: string;
  name: string;
  displayPosition: string;
  eligiblePositions: string[];
  editorialTeamAbbr: string;
  headshotUrl: string | null;
  status: string | null;
  injuryNote: string | null;
  stats: PlayerStatsDto | null;
}

export interface AvailablePlayer {
  playerId: string;
  name: string;
  displayPosition: string;
  eligiblePositions: string[];
  editorialTeamAbbr: string;
  headshotUrl: string | null;
  status: string | null;
  stats: PlayerStatsDto | null;
  percentOwned: number | null;
  deltaWeek: number | null;
}

export interface Standing {
  teamKey: string;
  name: string;
  logoUrl: string | null;
  rank: number;
  wins: number;
  losses: number;
  ties: number;
  pct: number;
  gamesBack: number;
  pointsFor: number;
  pointsAgainst: number;
  isMyTeam: boolean;
}

export interface MatchupStatDto {
  statName: string;
  myValue: number | null;
  opponentValue: number | null;
  winner: 'me' | 'opponent' | 'tied';
}

export interface MatchupDto {
  week: number;
  myTeam: TeamDto;
  opponent: TeamDto | null;
  stats: MatchupStatDto[];
  myWins: number;
  opponentWins: number;
  ties: number;
}

export interface CategoryRank {
  category: string;
  myValue: number;
  leagueAvg: number;
  rank: number;
  totalTeams: number;
}

export interface InsightsDto {
  myAverages: Record<string, number>;
  strongest: CategoryRank[];
  weakest: CategoryRank[];
}
