import {
  ArcElement,
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Title,
  Tooltip,
} from 'chart.js';
import { CHART_BG_COLOR } from './consts';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  PointElement,
  LineElement,
  ArcElement
);

ChartJS.defaults.color = '#AAAAAA';

export const liveChartOptions = {
  responsive: true,
  scales: {
    y: {
      ticks: {
        // Include a dollar sign in the ticks
        callback: function (value, index, ticks) {
          switch (value) {
            case 0:
              return 'RED';
            case 1:
              return 'YELLOW';
            case 2:
              return 'GREEN';
            default:
              return '';
          }
        },
      },
      min: 0,
      max: 3,
      grid: {
        color: CHART_BG_COLOR,
      },
    },
    x: {
      title: {
        display: true,
        text: 'Time (s)',
      },
      grid: {
        color: CHART_BG_COLOR,
      },
    },
  },
  animation: {
    duration: 0,
  },
  transitions: {
    active: 100,
  },
};

export const pieChartOptions = {
  responsive: true,
  backgroundColor: CHART_BG_COLOR,
  maintainAspectRatio: true,
  aspectRatio: 1,
};
