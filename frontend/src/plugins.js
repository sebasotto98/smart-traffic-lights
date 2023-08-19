import { CHART_BG_COLOR } from './consts';

export const customCanvasBackgroundColor = {
  id: 'custom_canvas_background_color',
  beforeDraw: (chart, args, options) => {
    const {
      ctx,
      chartArea: { top, left, width, height },
    } = chart;
    ctx.save();
    ctx.globalCompositeOperation = 'destination-over';
    ctx.fillStyle = CHART_BG_COLOR;
    ctx.fillRect(left, top, width, height);
    ctx.restore();
  },
};
