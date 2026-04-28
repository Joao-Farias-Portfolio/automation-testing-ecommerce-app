import axios, { type AxiosInstance } from "axios";
import type { HttpPort } from "../ports/httpPort";

export class AxiosHttpPort implements HttpPort {
  private readonly client: AxiosInstance;

  constructor(baseUrl: string) {
    this.client = axios.create({ baseURL: baseUrl });
  }

  async getAs<T>(path: string): Promise<T> {
    const response = await this.client.get<T>(path);
    return response.data;
  }

  async getListAs<T>(path: string): Promise<ReadonlyArray<T>> {
    const response = await this.client.get<T[]>(path);
    return response.data;
  }

  async getListWithQueryAs<T>(path: string, paramName: string, paramValue: string): Promise<ReadonlyArray<T>> {
    const response = await this.client.get<T[]>(path, { params: { [paramName]: paramValue } });
    return response.data;
  }
}
