export interface HttpPort {
  getAs<T>(path: string): Promise<T>;
  getListAs<T>(path: string): Promise<ReadonlyArray<T>>;
  getListWithQueryAs<T>(path: string, paramName: string, paramValue: string): Promise<ReadonlyArray<T>>;
}
