import type { TeamDto } from '../types';

interface Props {
  team: TeamDto | null;
}

export default function MyTeamPanel({ team }: Props) {
  if (!team) {
    return (
      <div className="card panel">
        <h2>My Team</h2>
        <p style={{ color: 'var(--text-muted)' }}>No team found</p>
      </div>
    );
  }

  const record = [team.wins ?? 0, team.losses ?? 0, team.ties ?? 0].join('-');

  return (
    <div className="card panel">
      <h2>My Team</h2>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ fontSize: '1.125rem', fontWeight: 600 }}>{team.name}</div>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
            {team.managerNickname}
          </div>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ display: 'flex', gap: '1.5rem' }}>
            {team.rank != null && (
              <div>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>RANK</div>
                <div style={{ fontSize: '1.25rem', fontWeight: 600 }}>#{team.rank}</div>
              </div>
            )}
            <div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>RECORD</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 600 }}>{record}</div>
            </div>
            <div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>WAIVER</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 600 }}>#{team.waiverPriority}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
