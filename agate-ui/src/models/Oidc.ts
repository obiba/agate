// Hand-written, not generated: mirrors OIDCAuthProviderSummaryDto of obiba-commons' OIDCDtos.proto, which the
// agate-ui build (Makefile, target "proto") does not generate TypeScript from. Keep in sync by hand.

export interface OIDCAuthProviderSummaryDto {
  name: string;
  title?: string | undefined;
  providerUrl?: string | undefined;
}
