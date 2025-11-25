import React, { useState, useEffect } from 'react';
import { TrendingUp, RefreshCw, DollarSign, AlertCircle, Activity } from 'lucide-react';

const SportsBettingArbBot = () => {
  const [opportunities, setOpportunities] = useState([]);
  const [isScanning, setIsScanning] = useState(false);
  const [stats, setStats] = useState({
    totalOpportunities: 0,
    avgROI: 0,
    totalProfit: 0
  });

  const fetchOpportunities = async () => {
    setIsScanning(true);
    try {
      const response = await fetch('http://localhost:8080/api/arbitrage/opportunities');
      const data = await response.json();
      
      setOpportunities(data);
      
      if (data.length > 0) {
        const totalROI = data.reduce((sum, opp) => sum + parseFloat(opp.roi), 0);
        const totalProfit = data.reduce((sum, opp) => sum + parseFloat(opp.estimatedProfit), 0);
        
        setStats({
          totalOpportunities: data.length,
          avgROI: (totalROI / data.length).toFixed(2),
          totalProfit: totalProfit.toFixed(2)
        });
      }
    } catch (error) {
      console.error('Error fetching opportunities:', error);
    }
    setIsScanning(false);
  };

  useEffect(() => {
    fetchOpportunities();
    const interval = setInterval(fetchOpportunities, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div style={{ minHeight: '100vh', background: 'linear-gradient(to bottom right, #0f172a, #1e3a8a, #0f172a)', padding: '24px' }}>
      <div style={{ maxWidth: '1280px', margin: '0 auto' }}>
        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <h1 style={{ fontSize: '36px', fontWeight: 'bold', color: 'white', marginBottom: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px' }}>
            <TrendingUp style={{ color: '#4ade80' }} size={40} />
            Sports Betting Arbitrage Bot
          </h1>
          <p style={{ color: '#bfdbfe' }}>Real-time arbitrage opportunity scanner with Kafka streaming</p>
        </div>

        {/* Stats Dashboard */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '16px', marginBottom: '32px' }}>
          <div style={{ background: 'rgba(255,255,255,0.1)', backdropFilter: 'blur(10px)', borderRadius: '8px', padding: '24px', border: '1px solid rgba(255,255,255,0.2)' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <p style={{ color: '#bfdbfe', fontSize: '14px', marginBottom: '4px' }}>Total Opportunities</p>
                <p style={{ fontSize: '30px', fontWeight: 'bold', color: 'white' }}>{stats.totalOpportunities}</p>
              </div>
              <Activity style={{ color: '#4ade80' }} size={32} />
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.1)', backdropFilter: 'blur(10px)', borderRadius: '8px', padding: '24px', border: '1px solid rgba(255,255,255,0.2)' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <p style={{ color: '#bfdbfe', fontSize: '14px', marginBottom: '4px' }}>Average ROI</p>
                <p style={{ fontSize: '30px', fontWeight: 'bold', color: '#4ade80' }}>{stats.avgROI}%</p>
              </div>
              <TrendingUp style={{ color: '#4ade80' }} size={32} />
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.1)', backdropFilter: 'blur(10px)', borderRadius: '8px', padding: '24px', border: '1px solid rgba(255,255,255,0.2)' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <p style={{ color: '#bfdbfe', fontSize: '14px', marginBottom: '4px' }}>Est. Profit ($1000 stake)</p>
                <p style={{ fontSize: '30px', fontWeight: 'bold', color: '#facc15' }}>${stats.totalProfit}</p>
              </div>
              <DollarSign style={{ color: '#facc15' }} size={32} />
            </div>
          </div>
        </div>

        {/* Scan Button */}
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '24px' }}>
          <button
            onClick={fetchOpportunities}
            disabled={isScanning}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              background: isScanning ? '#6b7280' : '#22c55e',
              color: 'white',
              padding: '12px 24px',
              borderRadius: '8px',
              fontWeight: '600',
              border: 'none',
              cursor: isScanning ? 'not-allowed' : 'pointer',
              boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)'
            }}
          >
            <RefreshCw className={isScanning ? 'spin' : ''} size={20} />
            {isScanning ? 'Scanning...' : 'Refresh Opportunities'}
          </button>
        </div>

        {/* Opportunities List */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {opportunities.length === 0 ? (
            <div style={{ background: 'rgba(255,255,255,0.1)', backdropFilter: 'blur(10px)', borderRadius: '8px', padding: '48px', textAlign: 'center', border: '1px solid rgba(255,255,255,0.2)' }}>
              <AlertCircle style={{ margin: '0 auto 16px', color: '#93c5fd' }} size={48} />
              <p style={{ color: 'white', fontSize: '18px' }}>No arbitrage opportunities found</p>
              <p style={{ color: '#bfdbfe', marginTop: '8px' }}>Send betting odds to Kafka to see opportunities</p>
            </div>
          ) : (
            opportunities.map((opp, index) => (
              <div key={index} style={{ background: 'rgba(255,255,255,0.1)', backdropFilter: 'blur(10px)', borderRadius: '8px', padding: '24px', border: '1px solid rgba(255,255,255,0.2)' }}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
                  <div>
                    <p style={{ color: '#bfdbfe', fontSize: '14px', marginBottom: '4px' }}>Event</p>
                    <p style={{ color: 'white', fontWeight: 'bold' }}>{opp.event}</p>
                    <p style={{ color: '#93c5fd', fontSize: '14px' }}>{opp.sport} • {opp.market}</p>
                  </div>

                  <div>
                    <p style={{ color: '#bfdbfe', fontSize: '14px', marginBottom: '4px' }}>Book 1: {opp.sportsbook1}</p>
                    <p style={{ color: 'white', fontSize: '20px', fontWeight: 'bold' }}>{opp.odds1}</p>
                    <p style={{ color: '#4ade80', fontSize: '14px' }}>Stake: {opp.stake1Percentage}%</p>
                  </div>

                  <div>
                    <p style={{ color: '#bfdbfe', fontSize: '14px', marginBottom: '4px' }}>Book 2: {opp.sportsbook2}</p>
                    <p style={{ color: 'white', fontSize: '20px', fontWeight: 'bold' }}>{opp.odds2}</p>
                    <p style={{ color: '#4ade80', fontSize: '14px' }}>Stake: {opp.stake2Percentage}%</p>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'flex-end' }}>
                    <div style={{ background: 'rgba(34, 197, 94, 0.2)', border: '1px solid #4ade80', borderRadius: '8px', padding: '8px 16px', textAlign: 'center' }}>
                      <p style={{ color: '#4ade80', fontSize: '14px' }}>ROI</p>
                      <p style={{ color: '#4ade80', fontSize: '24px', fontWeight: 'bold' }}>{opp.roi}%</p>
                    </div>
                    <p style={{ color: '#facc15', fontSize: '14px', marginTop: '8px' }}>Est. Profit: ${opp.estimatedProfit}</p>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Footer */}
        <div style={{ marginTop: '32px', background: 'rgba(59, 130, 246, 0.2)', border: '1px solid rgba(96, 165, 250, 0.3)', borderRadius: '8px', padding: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'start', gap: '12px' }}>
            <AlertCircle style={{ color: '#93c5fd', flexShrink: 0, marginTop: '4px' }} size={20} />
            <div style={{ fontSize: '14px', color: '#dbeafe' }}>
              <p style={{ fontWeight: '600', marginBottom: '4px' }}>Live Kafka Integration</p>
              <p>Connected to Java backend processing real-time betting odds via Kafka streams. Send JSON messages to the "betting-odds" topic to see arbitrage calculations.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SportsBettingArbBot;