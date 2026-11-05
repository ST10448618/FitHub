// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
/**
 * One error type for the whole API. Handlers throw ApiError; the HTTP layer turns it into
 * { "error": { code, message, details?, requestId } } so every failure has the same shape.
 */
export interface FieldError {
  field: string;
  message: string;
}

export class ApiError extends Error {
  // Written out in full (no constructor parameter properties) so Node can run
  // this file by stripping types, without a compile step.
  readonly status: number;
  readonly code: string;
  readonly details: FieldError[] | undefined;

  constructor(status: number, code: string, message: string, details?: FieldError[]) {
    super(message);
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export const validationFailed = (details: FieldError[], message = 'Some fields are invalid'): ApiError =>
  new ApiError(400, 'VALIDATION_FAILED', message, details);
export const unauthenticated = (): ApiError =>
  new ApiError(401, 'UNAUTHENTICATED', 'A valid Bearer token is required');
export const notFound = (what: string): ApiError =>
  new ApiError(404, 'NOT_FOUND', `${what} not found`);
export const forbidden = (code: string, message: string): ApiError =>
  new ApiError(403, code, message);
export const conflict = (code: string, message: string): ApiError =>
  new ApiError(409, code, message);
export const unprocessable = (code: string, message: string): ApiError =>
  new ApiError(422, code, message);