export class ApiError extends Error {
  status?: number;
  code?: string;
  data?: object;

  constructor(message: string, options?: {
    status?: number;
    code?: string;
    data?: object;
  }) {
    super(message);
    this.name = "ApiError";
    this.status = options?.status;
    this.code = options?.code;
    this.data = options?.data;
  }
}
