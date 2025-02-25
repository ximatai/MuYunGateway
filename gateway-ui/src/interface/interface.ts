// 定义基础类型接口
interface SSLConfig {
  use: boolean;
  certPath: string;
  keyPath: string;
}

interface LoginConfig {
  path: string;
  page: string;
  api: string;
}

interface JWTConfig {
  use: boolean;
  checkExpiration: boolean;
  expiresMin: number | null;
}

interface SessionConfig {
  use: boolean;
  timeoutHour: number | null;
}

interface RedirectRule {
  from: string;
  to: string;
}

interface FrontendConfig {
  path: string;
  comment: string;
  regex: boolean;
  dir: string;
  secured: boolean;
  notFoundReroute: string;
  allowlist: string[];
  noCache: string[];
}

interface BackendConfig {
  url: string;
  weight: number | null;
}

interface UpstreamConfig {
  path: string;
  secured: boolean;
  regex: boolean;
  comment: string | null;
  noCache: string[];
  allowlist: string[];
  backends: BackendConfig[];
}

// 主配置接口
export interface GatewayConfig {
  port: number | null;
  ssl: SSLConfig;
  index: string;
  login: LoginConfig;
  jwt: JWTConfig;
  session: SessionConfig;
  redirects: RedirectRule[];
  frontends: FrontendConfig[];
  upstreams: UpstreamConfig[];
}
