export interface Snippet {
  id?: number;
  filename: string;
  content: string;
  ordinal: number;
}

export interface Template {
  id: number;
  title: string;
  snippets: Snippet[];
  modifiedAt: string;
}

export interface TemplateListItem {
  id: number;
  title: string;
  thumbnailSnippet: {
    filename: string;
    thumbnailContent: string;
  };
  modifiedAt: string;
}

export interface TemplateListResponse {
  templates: TemplateListItem[];
}

export interface TemplateUploadRequest {
  title: string;
  snippets: Snippet[];
}

export interface TemplateEditRequest {
  title: string;
  createSnippets: Snippet[];
  updateSnippets: Snippet[];
  deleteSnippetIds: number[];
}
