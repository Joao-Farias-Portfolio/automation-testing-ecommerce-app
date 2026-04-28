import type { Channel } from "./channel";
import type { CatalogueProtocol } from "./catalogueProtocol";

type DriverFactory = () => CatalogueProtocol;

const registry = new Map<Channel, DriverFactory>();

export function register(channel: Channel, factory: DriverFactory): void {
  registry.set(channel, factory);
}

export function create(channel: Channel): CatalogueProtocol {
  const factory = registry.get(channel);
  if (factory === undefined) {
    throw new Error(
      `No driver registered for channel '${channel}'. Valid values: ${[...registry.keys()].join(", ")}`
    );
  }
  return factory();
}
