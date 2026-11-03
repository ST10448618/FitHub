/** One JSON object per line, easy to read in the Render log viewer. Never log tokens or request bodies. */
export interface Logger {
  info(message: string, fields?: Record<string, unknown>): void;
  warn(message: string, fields?: Record<string, unknown>): void;
  error(message: string, fields?: Record<string, unknown>): void;
}

const write = (level: string, message: string, fields?: Record<string, unknown>): void => {
  console.log(JSON.stringify({ time: new Date().toISOString(), level, message, ...fields }));
};

export const consoleLogger: Logger = {
  info: (m, f) => write('info', m, f),
  warn: (m, f) => write('warn', m, f),
  error: (m, f) => write('error', m, f),
};

export const silentLogger: Logger = { info() {}, warn() {}, error() {} };
