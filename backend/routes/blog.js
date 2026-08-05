
const express = require('express');
const { BlogPost } = require('../models');
const auth = require('../middleware/auth');
const adminAuth = require('../middleware/adminAuth');
const router = express.Router();

// Get published blog posts
router.get('/posts', auth, async (req, res) => {
  try {
    const posts = await BlogPost.findAll({
      where: { status: 'published' },
      order: [['createdAt', 'DESC']],
      attributes: ['id', 'title', 'excerpt', 'featured_image', 'createdAt']
    });

    res.json({
      success: true,
      data: posts
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Get single blog post
router.get('/posts/:id', auth, async (req, res) => {
  try {
    const post = await BlogPost.findOne({
      where: { 
        id: req.params.id,
        status: 'published'
      }
    });

    if (!post) {
      return res.status(404).json({
        success: false,
        message: 'Post not found'
      });
    }

    res.json({
      success: true,
      data: post
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Get all posts
router.get('/admin/posts', adminAuth, async (req, res) => {
  try {
    const posts = await BlogPost.findAll({
      order: [['createdAt', 'DESC']]
    });

    res.json({
      success: true,
      data: posts
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Create blog post
router.post('/admin/posts', adminAuth, async (req, res) => {
  try {
    const { title, content, excerpt, featured_image, status } = req.body;

    const post = await BlogPost.create({
      title,
      content,
      excerpt,
      featured_image,
      status: status || 'draft',
      author_id: req.user.id
    });

    res.status(201).json({
      success: true,
      message: 'Blog post created',
      data: post
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Update blog post
router.put('/admin/posts/:id', adminAuth, async (req, res) => {
  try {
    const { id } = req.params;
    const updateData = req.body;

    const post = await BlogPost.findByPk(id);
    if (!post) {
      return res.status(404).json({
        success: false,
        message: 'Post not found'
      });
    }

    await post.update(updateData);

    res.json({
      success: true,
      message: 'Blog post updated',
      data: post
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Delete blog post
router.delete('/admin/posts/:id', adminAuth, async (req, res) => {
  try {
    const { id } = req.params;

    const post = await BlogPost.findByPk(id);
    if (!post) {
      return res.status(404).json({
        success: false,
        message: 'Post not found'
      });
    }

    await post.destroy();

    res.json({
      success: true,
      message: 'Blog post deleted'
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

module.exports = router;
