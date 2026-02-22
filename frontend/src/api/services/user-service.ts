import apiClient from '../axios-client'
import type {UserProfile} from "../../types/UserProfile.ts";

const API_URL = '/api/v1/users';

export async function getProfile(id: string): Promise<UserProfile> {
  return apiClient.get(`${API_URL}/${id}`);
}

export async function createProfile(profile: UserProfile): Promise<UserProfile> {
  return apiClient.post(API_URL, profile);
}

