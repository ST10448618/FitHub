// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { ApiError, validationFailed } from './errors.ts';
import type { FieldError } from './errors.ts';

/**
 * Small, dependency-free input validator.
 *
 * Every API write is validated here before it touches the database. Problems
 * are collected (not thrown one by one) so the app can show all of them at once.
 * Call finish() at the end of the handler's validation section.
 *
 * Reference: https://json-schema.org (accessed February 2026) — the checks below
 * mirror the JSON-Schema constraints documented in openapi.yaml.
 */

const DATE_RE = /^(\d{4})-(\d{2})-(\d{2})$/;
// ISO-8601 with an explicit offset (Z or +hh:mm). Offsets are required so
// "newer/older" is unambiguous when resolving last-write-wins.
const TIMESTAMP_RE = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d{1,9})?)?(Z|[+-]\d{2}:\d{2})$/;
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
export const ID_RE = /^[A-Za-z0-9_:.-]{1,128}$/;

export function isPlainObject(v: unknown): v is Record<string, unknown> {
  return typeof v === 'object' && v !== null && !Array.isArray(v);
}

export function isValidDate(value: string): boolean {
  const m = DATE_RE.exec(value);
  if (!m) return false;
  const [y, mo, d] = [Number(m[1]), Number(m[2]), Number(m[3])];
  const dt = new Date(Date.UTC(y, mo - 1, d));
  return dt.getUTCFullYear() === y && dt.getUTCMonth() === mo - 1 && dt.getUTCDate() === d;
}

/** Firestore forbids the ids "." and ".." and anything that looks like __name__. */
export function isSafeId(id: string): boolean {
  return ID_RE.test(id) && id !== '.' && id !== '..' && !id.startsWith('__');
}

export function assertValidId(id: string, field = 'id'): string {
  if (!isSafeId(id)) {
    throw validationFailed([{ field, message: 'must be 1-128 characters: letters, digits, _ - : .' }]);
  }
  return id;
}

interface StrOpts { min?: number; max?: number }
interface NumOpts { min?: number; max?: number; int?: boolean; exclusiveMin?: boolean }

export class Validator {
  readonly errors: FieldError[] = [];
  private readonly src: Record<string, unknown>;
  private readonly prefix: string;

  constructor(input: unknown, prefix = '') {
    if (!isPlainObject(input)) {
      throw new ApiError(400, 'VALIDATION_FAILED', 'Request body must be a JSON object', [
        { field: prefix || 'body', message: 'must be a JSON object' },
      ]);
    }
    this.src = input;
    this.prefix = prefix;
  }

  private fail(field: string, message: string): void {
    this.errors.push({ field: this.prefix + field, message });
  }

  private raw(field: string): unknown {
    return this.src[field];
  }

  // ---- strings ----
  /** Required string. Trimmed. A blank string counts as missing when min >= 1. */
  str(field: string, o: StrOpts = {}): string {
    const v = this.raw(field);
    if (typeof v !== 'string') {
      this.fail(field, 'is required and must be text');
      return '';
    }
    const t = v.trim();
    const min = o.min ?? 0;
    if (t.length < min) this.fail(field, min === 1 ? 'must not be blank' : `must be at least ${min} characters`);
    if (o.max !== undefined && t.length > o.max) this.fail(field, `must be at most ${o.max} characters`);
    return t;
  }

  /** Optional string. undefined = absent, null = explicitly empty. */
  optStr(field: string, o: StrOpts = {}): string | null | undefined {
    const v = this.raw(field);
    if (v === undefined || v === null) return v;
    return this.str(field, o);
  }

  email(field: string): string | undefined {
    const v = this.optStr(field, { max: 254 });
    if (typeof v === 'string' && v !== '' && !EMAIL_RE.test(v)) {
      this.fail(field, 'must be a valid email address');
      return undefined;
    }
    return v === null || v === '' ? undefined : v;
  }

  // ---- numbers ----
  num(field: string, o: NumOpts = {}): number {
    const v = this.raw(field);
    if (typeof v !== 'number' || !Number.isFinite(v)) {
      this.fail(field, 'is required and must be a number');
      return 0;
    }
    if (o.int && !Number.isInteger(v)) this.fail(field, 'must be a whole number');
    if (o.min !== undefined) {
      if (o.exclusiveMin ? v <= o.min : v < o.min) {
        this.fail(field, o.exclusiveMin ? `must be greater than ${o.min}` : `must be at least ${o.min}`);
      }
    }
    if (o.max !== undefined && v > o.max) this.fail(field, `must be at most ${o.max}`);
    return v;
  }

  /** Optional number. null means "unavailable" (not zero). */
  optNum(field: string, o: NumOpts = {}): number | null | undefined {
    const v = this.raw(field);
    if (v === undefined || v === null) return v;
    return this.num(field, o);
  }

  // ---- booleans / enums ----
  bool(field: string): boolean {
    const v = this.raw(field);
    if (typeof v !== 'boolean') {
      this.fail(field, 'is required and must be true or false');
      return false;
    }
    return v;
  }

  optBool(field: string): boolean | undefined {
    const v = this.raw(field);
    if (v === undefined) return undefined;
    return this.bool(field);
  }

  oneOf<T extends string>(field: string, values: readonly T[]): T {
    const v = this.raw(field);
    if (typeof v !== 'string' || !(values as readonly string[]).includes(v)) {
      this.fail(field, `must be one of: ${values.join(', ')}`);
      return values[0] as T;
    }
    return v as T;
  }

  optOneOf<T extends string>(field: string, values: readonly T[]): T | undefined {
    if (this.raw(field) === undefined) return undefined;
    return this.oneOf(field, values);
  }

  // ---- dates ----
  /** YYYY-MM-DD, must be a real calendar date. */
  date(field: string): string {
    const v = this.raw(field);
    if (typeof v !== 'string' || !isValidDate(v)) {
      this.fail(field, 'must be a valid date in the form YYYY-MM-DD');
      return '1970-01-01';
    }
    return v;
  }

  optDate(field: string): string | undefined {
    if (this.raw(field) === undefined || this.raw(field) === null) return undefined;
    return this.date(field);
  }

  /** ISO-8601 timestamp with offset. Returned normalised to UTC ("...Z"). */
  timestamp(field: string): string {
    const v = this.raw(field);
    if (typeof v !== 'string' || v.length > 40 || !TIMESTAMP_RE.test(v) || Number.isNaN(Date.parse(v))) {
      this.fail(field, 'must be an ISO-8601 timestamp with a time zone, e.g. 2026-10-30T12:00:00Z');
      return new Date(0).toISOString();
    }
    return new Date(v).toISOString();
  }

  // ---- arrays ----
  list(field: string, o: { min?: number; max?: number } = {}): unknown[] {
    const v = this.raw(field);
    if (!Array.isArray(v)) {
      this.fail(field, 'is required and must be a list');
      return [];
    }
    if (o.min !== undefined && v.length < o.min) this.fail(field, `must contain at least ${o.min} item(s)`);
    if (o.max !== undefined && v.length > o.max) this.fail(field, `must contain at most ${o.max} item(s)`);
    return v;
  }

  /** Child validator for one element of a list, so error paths read like exercises[2].reps. */
  child(value: unknown, field: string, index: number): Validator {
    return new Validator(value, `${this.prefix}${field}[${index}].`);
  }

  /** Merge a child validator's problems into this one. */
  absorb(child: Validator): void {
    this.errors.push(...child.errors);
  }

  /** Throws one VALIDATION_FAILED error listing every problem found. */
  finish(): void {
    if (this.errors.length > 0) throw validationFailed(this.errors);
  }
}