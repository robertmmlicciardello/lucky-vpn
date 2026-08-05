
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { PenTool, Eye, Edit, Trash2, Plus, Calendar, User } from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";

interface BlogPost {
  id: number;
  title: string;
  content: string;
  excerpt: string;
  author: string;
  status: 'draft' | 'published' | 'archived';
  category: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  views: number;
}

const BlogManagement = () => {
  const { toast } = useToast();
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingPost, setEditingPost] = useState<BlogPost | null>(null);
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    excerpt: '',
    category: '',
    tags: '',
    status: 'draft' as BlogPost['status']
  });

  const [posts, setPosts] = useState<BlogPost[]>([
    {
      id: 1,
      title: "How to Choose the Best VPN Server",
      content: "When selecting a VPN server, consider factors like location, speed, and security protocols...",
      excerpt: "Learn how to choose the best VPN server for your needs",
      author: "Admin",
      status: 'published',
      category: 'Guide',
      tags: ['VPN', 'Security', 'Guide'],
      createdAt: '2024-06-10 10:00',
      updatedAt: '2024-06-10 10:00',
      views: 1250
    },
    {
      id: 2,
      title: "New Premium Features Available",
      content: "We're excited to announce new premium features including...",
      excerpt: "Discover the latest premium features in our VPN app",
      author: "Admin",
      status: 'published',
      category: 'News',
      tags: ['Premium', 'Features', 'Update'],
      createdAt: '2024-06-08 15:30',
      updatedAt: '2024-06-08 15:30',
      views: 890
    },
    {
      id: 3,
      title: "Server Maintenance Schedule",
      content: "Upcoming maintenance schedule for our servers...",
      excerpt: "Important information about server maintenance",
      author: "Admin",
      status: 'draft',
      category: 'Announcement',
      tags: ['Maintenance', 'Servers'],
      createdAt: '2024-06-12 09:00',
      updatedAt: '2024-06-12 09:00',
      views: 0
    }
  ]);

  const categories = ['Guide', 'News', 'Announcement', 'Tutorial', 'Tips'];

  const handleSubmit = () => {
    if (!formData.title.trim() || !formData.content.trim()) {
      toast({
        title: "Error",
        description: "Please fill in title and content",
        variant: "destructive"
      });
      return;
    }

    const tagsArray = formData.tags.split(',').map(tag => tag.trim()).filter(tag => tag);
    
    if (editingPost) {
      // Update existing post
      setPosts(posts.map(post => 
        post.id === editingPost.id 
          ? {
              ...post,
              ...formData,
              tags: tagsArray,
              updatedAt: new Date().toLocaleString()
            }
          : post
      ));
      setEditingPost(null);
      toast({
        title: "Success",
        description: "Blog post updated successfully"
      });
    } else {
      // Create new post
      const newPost: BlogPost = {
        id: Date.now(),
        ...formData,
        tags: tagsArray,
        author: 'Admin',
        createdAt: new Date().toLocaleString(),
        updatedAt: new Date().toLocaleString(),
        views: 0
      };
      setPosts([newPost, ...posts]);
      toast({
        title: "Success",
        description: "Blog post created successfully"
      });
    }

    // Reset form
    setFormData({
      title: '',
      content: '',
      excerpt: '',
      category: '',
      tags: '',
      status: 'draft'
    });
    setShowCreateForm(false);
  };

  const handleEdit = (post: BlogPost) => {
    setEditingPost(post);
    setFormData({
      title: post.title,
      content: post.content,
      excerpt: post.excerpt,
      category: post.category,
      tags: post.tags.join(', '),
      status: post.status
    });
    setShowCreateForm(true);
  };

  const handleDelete = (postId: number) => {
    setPosts(posts.filter(post => post.id !== postId));
    toast({
      title: "Success",
      description: "Blog post deleted successfully"
    });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'published': return 'bg-green-500';
      case 'draft': return 'bg-yellow-500';
      case 'archived': return 'bg-gray-500';
      default: return 'bg-gray-500';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Blog Management</h2>
          <p className="text-gray-600">Create and manage blog posts for your users</p>
        </div>
        <Button onClick={() => setShowCreateForm(true)}>
          <Plus className="w-4 h-4 mr-2" />
          New Post
        </Button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-blue-600">{posts.length}</div>
            <div className="text-sm text-gray-600">Total Posts</div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-green-600">{posts.filter(p => p.status === 'published').length}</div>
            <div className="text-sm text-gray-600">Published</div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-yellow-600">{posts.filter(p => p.status === 'draft').length}</div>
            <div className="text-sm text-gray-600">Drafts</div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-purple-600">{posts.reduce((sum, post) => sum + post.views, 0)}</div>
            <div className="text-sm text-gray-600">Total Views</div>
          </CardContent>
        </Card>
      </div>

      {/* Create/Edit Form */}
      {showCreateForm && (
        <Card>
          <CardHeader>
            <CardTitle>{editingPost ? 'Edit Post' : 'Create New Post'}</CardTitle>
            <CardDescription>
              {editingPost ? 'Update your blog post' : 'Create a new blog post for your users'}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium mb-2 block">Title</label>
                <Input
                  placeholder="Post title..."
                  value={formData.title}
                  onChange={(e) => setFormData({...formData, title: e.target.value})}
                />
              </div>
              <div>
                <label className="text-sm font-medium mb-2 block">Category</label>
                <Select value={formData.category} onValueChange={(value) => setFormData({...formData, category: value})}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select category" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map(category => (
                      <SelectItem key={category} value={category}>{category}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div>
              <label className="text-sm font-medium mb-2 block">Excerpt</label>
              <Textarea
                placeholder="Brief description of the post..."
                value={formData.excerpt}
                onChange={(e) => setFormData({...formData, excerpt: e.target.value})}
                rows={2}
              />
            </div>

            <div>
              <label className="text-sm font-medium mb-2 block">Content</label>
              <Textarea
                placeholder="Write your blog post content..."
                value={formData.content}
                onChange={(e) => setFormData({...formData, content: e.target.value})}
                rows={8}
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium mb-2 block">Tags (comma separated)</label>
                <Input
                  placeholder="VPN, Security, Guide..."
                  value={formData.tags}
                  onChange={(e) => setFormData({...formData, tags: e.target.value})}
                />
              </div>
              <div>
                <label className="text-sm font-medium mb-2 block">Status</label>
                <Select value={formData.status} onValueChange={(value: BlogPost['status']) => setFormData({...formData, status: value})}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="draft">Draft</SelectItem>
                    <SelectItem value="published">Published</SelectItem>
                    <SelectItem value="archived">Archived</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="flex gap-2">
              <Button onClick={handleSubmit}>
                <PenTool className="w-4 h-4 mr-2" />
                {editingPost ? 'Update Post' : 'Create Post'}
              </Button>
              <Button variant="outline" onClick={() => {
                setShowCreateForm(false);
                setEditingPost(null);
                setFormData({
                  title: '',
                  content: '',
                  excerpt: '',
                  category: '',
                  tags: '',
                  status: 'draft'
                });
              }}>
                Cancel
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Posts List */}
      <Card>
        <CardHeader>
          <CardTitle>All Posts</CardTitle>
          <CardDescription>Manage your blog posts</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {posts.map((post) => (
              <div key={post.id} className="border rounded-lg p-4 hover:bg-gray-50">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <h3 className="font-semibold text-gray-900">{post.title}</h3>
                      <div className={`w-3 h-3 rounded-full ${getStatusColor(post.status)}`}></div>
                      <Badge variant="secondary">{post.category}</Badge>
                    </div>
                    <p className="text-gray-600 text-sm mb-2">{post.excerpt}</p>
                    <div className="flex items-center gap-4 text-xs text-gray-500 mb-2">
                      <span className="flex items-center gap-1">
                        <User className="w-3 h-3" />
                        {post.author}
                      </span>
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        {post.createdAt}
                      </span>
                      <span className="flex items-center gap-1">
                        <Eye className="w-3 h-3" />
                        {post.views} views
                      </span>
                    </div>
                    <div className="flex gap-1">
                      {post.tags.map(tag => (
                        <Badge key={tag} variant="outline" className="text-xs">
                          {tag}
                        </Badge>
                      ))}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button size="sm" variant="outline" onClick={() => handleEdit(post)}>
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button size="sm" variant="destructive" onClick={() => handleDelete(post.id)}>
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default BlogManagement;
