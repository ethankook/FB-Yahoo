import type { Standing } from '../types';

interface Props {
  standings: Standing[];
}

export default function StandingsTable({ standings }: Props) {
  return (
    <div className="card panel">
      <h2>Standings</h2>
      <div style={{ overflowX: 'auto' }}>
        <table>
          <thead>
            <tr>
              <th>Rank</th>
              <th>Team</th>
              <th>W</th>
              <th>L</th>
              <th>T</th>
              <th>PCT</th>
              <th>GB</th>
              <th>PF</th>
              <th>PA</th>
            </tr>
          </thead>
          <tbody>
            {standings.map(s => (
              <tr key={s.teamKey} className={s.isMyTeam ? 'my-team' : ''}>
                <td>{s.rank}</td>
                <td>{s.name}</td>
                <td>{s.wins}</td>
                <td>{s.losses}</td>
                <td>{s.ties}</td>
                <td>{s.pct != null ? Number(s.pct).toFixed(3) : '-'}</td>
                <td>{s.gamesBack ?? '-'}</td>
                <td>{s.pointsFor ?? '-'}</td>
                <td>{s.pointsAgainst ?? '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
