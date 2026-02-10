import { useState } from 'react';
import type { RosterPlayer } from '../types';

interface Props {
  roster: RosterPlayer[];
}

type SortKey = 'name' | 'pts' | 'reb' | 'ast' | 'stl' | 'blk';

export default function RosterTable({ roster }: Props) {
  const [sortKey, setSortKey] = useState<SortKey>('pts');
  const [sortAsc, setSortAsc] = useState(false);

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortAsc(!sortAsc);
    } else {
      setSortKey(key);
      setSortAsc(false);
    }
  };

  const sorted = [...roster].sort((a, b) => {
    let aVal: number | string = 0;
    let bVal: number | string = 0;

    if (sortKey === 'name') {
      aVal = a.name || '';
      bVal = b.name || '';
      return sortAsc ? aVal.localeCompare(bVal as string) : (bVal as string).localeCompare(aVal as string);
    }

    const statMap: Record<string, (r: RosterPlayer) => number> = {
      pts: r => r.stats?.ptsPg ?? 0,
      reb: r => r.stats?.rebPg ?? 0,
      ast: r => r.stats?.astPg ?? 0,
      stl: r => r.stats?.stlPg ?? 0,
      blk: r => r.stats?.blkPg ?? 0,
    };

    aVal = statMap[sortKey](a);
    bVal = statMap[sortKey](b);
    return sortAsc ? (aVal as number) - (bVal as number) : (bVal as number) - (aVal as number);
  });

  const sortIndicator = (key: SortKey) => {
    if (sortKey !== key) return '';
    return sortAsc ? ' \u25B2' : ' \u25BC';
  };

  const statusBadge = (status: string | null) => {
    if (!status || status === 'Healthy') return null;
    const cls = status === 'INJ' || status === 'O' ? 'inj' : status === 'DTD' ? 'dtd' : 'out';
    return <span className={`status-badge ${cls}`}>{status}</span>;
  };

  return (
    <div className="card panel">
      <h2>Roster</h2>
      <div style={{ overflowX: 'auto' }}>
        <table>
          <thead>
            <tr>
              <th className="sortable" onClick={() => handleSort('name')}>Player{sortIndicator('name')}</th>
              <th>Pos</th>
              <th>Team</th>
              <th>Status</th>
              <th className="sortable" onClick={() => handleSort('pts')}>PTS{sortIndicator('pts')}</th>
              <th className="sortable" onClick={() => handleSort('reb')}>REB{sortIndicator('reb')}</th>
              <th className="sortable" onClick={() => handleSort('ast')}>AST{sortIndicator('ast')}</th>
              <th className="sortable" onClick={() => handleSort('stl')}>STL{sortIndicator('stl')}</th>
              <th className="sortable" onClick={() => handleSort('blk')}>BLK{sortIndicator('blk')}</th>
            </tr>
          </thead>
          <tbody>
            {sorted.map(p => (
              <tr key={p.playerId}>
                <td>{p.name}</td>
                <td>{p.displayPosition}</td>
                <td>{p.editorialTeamAbbr}</td>
                <td>{statusBadge(p.status)} {p.injuryNote && <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{p.injuryNote}</span>}</td>
                <td>{p.stats?.ptsPg?.toFixed(1) ?? '-'}</td>
                <td>{p.stats?.rebPg?.toFixed(1) ?? '-'}</td>
                <td>{p.stats?.astPg?.toFixed(1) ?? '-'}</td>
                <td>{p.stats?.stlPg?.toFixed(1) ?? '-'}</td>
                <td>{p.stats?.blkPg?.toFixed(1) ?? '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
