import { SortFilter } from "@/model/Filter";

export interface Log {
  id: number;
  timestamp: string;
  level: string;
  message?: string;
  action?: string;
  username?: string;
  durationMs?: number;
  requestId?: string;
}

export interface LogRequest {
  search?: string;
  levels?: string[];
  actions?: string[];
  from?: string;
  to?: string;
  sort: SortFilter[];
}
